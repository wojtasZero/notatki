from fastapi import FastAPI, WebSocket, WebSocketDisconnect
import uvicorn
import socket

def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
    finally:
        s.close()
    return ip

print("Local IP:", get_local_ip())

app = FastAPI()

if __name__ == "__main__":
    uvicorn.run("server:app", host="0.0.0.0", port=8000, reload=True)

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

@app.websocket("/ws/notatki/{share_id}")
async def websocket_endpoint(websocket: WebSocket, share_id: str):
    await manager.connect(websocket, share_id)
    try:
        while True:
            data = await websocket.receive_text()
            await manager.broadcast(data, share_id, sender=websocket)
    except WebSocketDisconnect:
        manager.disconnect(websocket, share_id)