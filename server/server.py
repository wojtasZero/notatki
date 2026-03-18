from flask import Flask, request, jsonify
from flask_cors import CORS
import json
import uuid
import os
import socket

app = Flask(__name__)
# Enable CORS so the HTML file can make requests to this server
CORS(app) 

DATA_FILE = 'data.json'

def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        # Doesn't actually send data
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
    except Exception:
        ip = "127.0.0.1"
    finally:
        s.close()
    return ip

# Helper functions to read/write JSON data
def load_data():
    if os.path.exists(DATA_FILE):
        with open(DATA_FILE, 'r') as f:
            return json.load(f)
    return {"backups": {}, "shares": {}}

def save_data(data):
    with open(DATA_FILE, 'w') as f:
        json.dump(data, f)

# --- ENDPOINTS ---

@app.route('/backup', methods=['POST'])
def backup():
    req = request.json
    user_id = req.get('user_id')
    content = req.get('content')
    
    data = load_data()
    data["backups"][user_id] = content
    save_data(data)
    
    return "Notatka zapisana dla " + user_id

@app.route('/backup/<user_id>', methods=['GET'])
def get_backup(user_id):
    data = load_data()
    content = data["backups"].get(user_id)
    if content is None: 
        return "Nie znaleziono!"
    return content

@app.route('/share', methods=['POST'])
def share():
    content = request.json.get('content', '')
    # Generate a short unique ID for sharing
    user_id = request.json.get('user_id', '')
    
    data = load_data()
    data["shares"][user_id] = content
    save_data(data)
    
    return "Notatka udostępniona dla " + user_id

@app.route('/share/<share_id>', methods=['GET'])
def get_share(share_id):
    data = load_data()
    content = data["shares"].get(share_id)
    if content is None: 
        return "Nie znaleziono!"
    return content

if __name__ == '__main__':
    local_ip = get_local_ip()
    print(f"Starting server on:")
    print(f"  http://127.0.0.1:5000")
    print(f"  http://{local_ip}:5000")

    app.run(host="0.0.0.0", port=5000, debug=True)