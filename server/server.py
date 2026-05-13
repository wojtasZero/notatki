from fastapi import FastAPI, WebSocket, WebSocketDisconnect, Header
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import Optional
import bcrypt
import uvicorn
import socket
import sqlite3
import time
import uuid

DB_NAME = "notes.db"
GUEST = "Gość"


def get_password_hash(password: str) -> str:
    pwd_bytes = password.encode('utf-8')
    salt = bcrypt.gensalt()
    hashed_bytes = bcrypt.hashpw(pwd_bytes, salt)
    return hashed_bytes.decode('utf-8')

def verify_password(plain_password: str, hashed_password: str) -> bool:
    try:
        plain_bytes = plain_password.encode('utf-8')
        hashed_bytes = hashed_password.encode('utf-8')
        return bcrypt.checkpw(plain_bytes, hashed_bytes)
    except:
        return False

def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
    finally:
        s.close()
    return ip

print("Lokalne IP:", get_local_ip())

def init_db():
    with sqlite3.connect(DB_NAME) as conn:
        cur = conn.cursor()
        
        cur.execute("""
        CREATE TABLE IF NOT EXISTS notes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id TEXT,
            content TEXT,
            timestamp INTEGER,
            share_id TEXT UNIQUE
        )
        """)
        
        cur.execute("""
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE,
            password_hash TEXT,
            session_uuid TEXT
        )
        """)

        cur.execute("""
        CREATE TABLE IF NOT EXISTS note_access (
            note_id INTEGER,
            user_id TEXT,
            PRIMARY KEY (note_id, user_id)
        )
        """)

        conn.commit()

init_db()

app = FastAPI()

class AuthRequest(BaseModel):
    username: str
    password: str

class NoteRequest(BaseModel):
    username: str
    session_id: str  #do weryfikacji
    content: str
    note_id: Optional[int] = -1

class InviteRequest(BaseModel):
    note_id: int
    target_username: str

class ConnectionManager:
    def __init__(self):
        self.active_connections: dict[str, list[WebSocket]] = {}

    async def connect(self, websocket: WebSocket, share_id: str):
        await websocket.accept()
        if share_id not in self.active_connections:
            self.active_connections[share_id] = []
        self.active_connections[share_id].append(websocket)

    def disconnect(self, websocket: WebSocket, share_id: str):
        self.active_connections[share_id].remove(websocket)
        if not self.active_connections[share_id]:
            del self.active_connections[share_id]

    async def broadcast(self, message: str, share_id: str, sender: WebSocket):
        for connection in self.active_connections.get(share_id, []):
            if connection != sender:
                await connection.send_text(message)

manager = ConnectionManager()

def is_session_valid(username: str, session_id: str) -> bool:
    with sqlite3.connect(DB_NAME) as conn:
        cur = conn.cursor()
        user = cur.execute(
            "SELECT 1 FROM users WHERE username=? AND session_uuid=?", 
            (username, session_id)
        ).fetchone()
        return user is not None

@app.post("/register")
async def register(req: AuthRequest):
    if req.username == GUEST or len(req.username) < 3:
        return JSONResponse(status_code=400, content={"status": "error", "message": "Niedozwolona nazwa użytkownika"})
    
    hashed_password = get_password_hash(req.password)
    
    with sqlite3.connect(DB_NAME) as conn:
        cur = conn.cursor()
        try:
            cur.execute(
                "INSERT INTO users (username, password_hash) VALUES (?, ?)",
                (req.username, hashed_password)
            )
            conn.commit()
        except sqlite3.IntegrityError:
            return JSONResponse(status_code=400, content={"status": "error", "message": "Użytkownik już istnieje!"})
            
    return {"status": "ok", "message": "Zarejestrowano pomyślnie!"}

@app.post("/login")
async def login(req: AuthRequest):
    with sqlite3.connect(DB_NAME) as conn:
        cur = conn.cursor()
        user = cur.execute("SELECT password_hash FROM users WHERE username=?", (req.username,)).fetchone()
        
        if user and verify_password(req.password, user[0]):
            new_session_id = str(uuid.uuid4())
            cur.execute("UPDATE users SET session_uuid=? WHERE username=?", (new_session_id, req.username))
            conn.commit()
            return {"status": "ok", "session_id": new_session_id, "message": "Zalogowano!"}
            
    return JSONResponse(status_code=401, content={"status": "error", "message": "Błędny login lub hasło!"})

@app.post("/logout")
async def logout(req: AuthRequest):
    with sqlite3.connect(DB_NAME) as conn:
        cur = conn.cursor()
        cur.execute("UPDATE users SET session_uuid=NULL WHERE username=?", (req.username,))
        conn.commit()
    return {"status": "ok", "message": "Wylogowano"}


@app.post("/note/invite")
async def invite_user(req: InviteRequest, username: str = Header(default=None), x_session_id: str = Header(default=None)):
    if not is_session_valid(username, x_session_id):
        return JSONResponse(status_code=401, content={"status": "error", "message": "Brak autoryzacji"})

    with sqlite3.connect(DB_NAME) as conn:
        cur = conn.cursor()
        
        note = cur.execute("SELECT id, share_id FROM notes WHERE id=? AND user_id=?", (req.note_id, username)).fetchone()
        if not note:
            return JSONResponse(status_code=403, content={"status": "error", "message": "Możesz udostępniać tylko swoje notatki!"})

        share_id = note[1]

        if not share_id:
            share_id = str(uuid.uuid4())
            cur.execute("UPDATE notes SET share_id=? WHERE id=?", (share_id, req.note_id))

        target_user = cur.execute("SELECT id FROM users WHERE username=?", (req.target_username,)).fetchone()
        if not target_user:
            return JSONResponse(status_code=404, content={"status": "error", "message": "Nie znaleziono użytkownika!"})

        try:
            cur.execute("INSERT INTO note_access (note_id, user_id) VALUES (?, ?)", (req.note_id, req.target_username))
            conn.commit()
        except sqlite3.IntegrityError:
            return {"status": "ok", "message": "Ten użytkownik ma już dostęp do notatki."}

    return {"status": "ok", "share_id": share_id, "message": f"Udostępniono dla: {req.target_username}"}

@app.websocket("/ws/notatki/{share_id}")
async def websocket_endpoint(websocket: WebSocket, share_id: str):
    await manager.connect(websocket, share_id)
    try:
        while True:
            data = await websocket.receive_text()
            await manager.broadcast(data, share_id, sender=websocket)
    except WebSocketDisconnect:
        manager.disconnect(websocket, share_id)

@app.post("/note")
async def save_note(note: NoteRequest):
    if not is_session_valid(note.username, note.session_id):
        return JSONResponse(status_code=401, content={"status": "error", "message": "Brak dostępu!"})

    timestamp = int(time.time() * 1000)

    with sqlite3.connect(DB_NAME) as conn:
        cur = conn.cursor()

        if note.note_id is None or note.note_id == -1:
            cur.execute(
                "INSERT INTO notes(user_id, content, timestamp) VALUES (?, ?, ?)",
                (note.username, note.content, timestamp)
            )
            new_note_id = cur.lastrowid
            conn.commit()
            
            return {
                "status": "ok", 
                "message": "Utworzono nową notatkę!",
                "extra": new_note_id
            }
            
        else:
            has_access = cur.execute("""
                SELECT 1 FROM notes WHERE id=? AND user_id=?
                UNION
                SELECT 1 FROM note_access WHERE note_id=? AND user_id=?
            """, (note.note_id, note.username, note.note_id, note.username)).fetchone()

            if not has_access:
                return JSONResponse(status_code=403, content={"status": "error", "message": "Nie możesz edytować tej notatki!"})

            cur.execute(
                "UPDATE notes SET content=?, timestamp=? WHERE id=?",
                (note.content, timestamp, note.note_id)
            )
            conn.commit()

            return {
                "status": "ok", 
                "message": "Notatka zaktualizowana!",
                "extra": note.note_id
            }

@app.get("/notes/{username}")
async def load_notes(username: str, x_session_id: str = Header(default=None)):
    if not is_session_valid(username, x_session_id):
        return JSONResponse(status_code=401, content={"status": "error", "message": "Brak dostępu lub sesja wygasła!"})
    
    with sqlite3.connect(DB_NAME) as conn:
        cur = conn.cursor()
        rows = cur.execute("""
            SELECT id, content, timestamp, share_id
            FROM notes 
            WHERE user_id=?
            
            UNION
            
            SELECT n.id, n.content, n.timestamp, n.share_id
            FROM notes n
            JOIN note_access a ON n.id = a.note_id
            WHERE a.user_id=?
            
            ORDER BY timestamp DESC
        """, (username, username)).fetchall()

    notes = [{
        "id": row[0], 
        "content": row[1], 
        "timestamp": row[2], 
        "share_id": row[3]
    } for row in rows]
    
    return JSONResponse(content=notes)


@app.delete("/note/{note_id}")
async def delete_note(note_id: int, username: str = Header(default=None), x_session_id: str = Header(default=None)):
    if not username or not x_session_id or not is_session_valid(username, x_session_id):
        return JSONResponse(status_code=401, content={"status": "error", "message": "Brak dostępu!"})

    with sqlite3.connect(DB_NAME) as conn:
        cur = conn.cursor()
        note = cur.execute("SELECT user_id FROM notes WHERE id=?", (note_id,)).fetchone()
        
        if not note or note[0] != username:
            return JSONResponse(status_code=403, content={"status": "error", "message": "Nie możesz usunąć nieswojej notatki!"})

        cur.execute("DELETE FROM notes WHERE id=?", (note_id,))
        conn.commit()

    return {"status": "ok", "message": "Notatka usunięta!"}


if __name__ == "__main__":
    uvicorn.run("server:app", host="0.0.0.0", port=8000, reload=True)