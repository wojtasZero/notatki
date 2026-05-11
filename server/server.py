from fastapi import FastAPI, WebSocket, WebSocketDisconnect, Header, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel
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
    except ValueError:
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
            timestamp INTEGER
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
        conn.commit()

init_db()

app = FastAPI()

class AuthRequest(BaseModel):
    username: str
    password: str

class NoteRequest(BaseModel):
    username: str
    session_id: str  # Wymagamy UUID do zapisu
    content: str

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
        return JSONResponse(status_code=401, content={"status": "error", "message": "Brak dostępu lub sesja wygasła!"})

    timestamp = int(time.time() * 1000)

    with sqlite3.connect(DB_NAME) as conn:
        cur = conn.cursor()
        cur.execute(
            "INSERT INTO notes(user_id, content, timestamp) VALUES (?, ?, ?)",
            (note.username, note.content, timestamp)
        )
        conn.commit()

    return {"status": "ok", "message": "Notatka zapisana!"}

@app.get("/notes/{username}")
async def load_notes(username: str, x_session_id: str = Header(default=None)):
    if not x_session_id or not is_session_valid(username, x_session_id):
        return JSONResponse(status_code=401, content={"status": "error", "message": "Brak dostępu lub sesja wygasła!"})
    
    with sqlite3.connect(DB_NAME) as conn:
        cur = conn.cursor()
        rows = cur.execute(
            "SELECT id, content, timestamp FROM notes WHERE user_id=? ORDER BY timestamp DESC",
            (username,)
        ).fetchall()

    notes = [{"id": row[0], "content": row[1], "timestamp": row[2]} for row in rows]
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