import sqlite3InitModule from '@sqlite.org/sqlite-wasm';

let sqlite3 = null;

const databases = new Map();
const statements = new Map();

let nextDatabaseId = 0;
let nextStatementId = 0;

function openRequest(id, requestData) {
    try {
        const newDatabaseId = nextDatabaseId++;
        const oo1 = sqlite3.oo1;
        const path = requestData.path || requestData.fileName || 'temp.db';
        let newDatabase;
        if (oo1.OpfsDb) {
            newDatabase = new oo1.OpfsDb(path);
        } else {
            newDatabase = new oo1.DB(path, 'ct');
        }
        databases.set(newDatabaseId, newDatabase);
        const result = { 'databaseId': newDatabaseId };
        postMessage({ 'id': id, 'result': result });
    } catch (error) {
        postMessage({ 'id': id, 'error': { 'message': error.message } });
    }
}

function prepareRequest(id, requestData) {
    try {
        const newStatementId = nextStatementId++;
        const database = databases.get(requestData.databaseId);
        if (!database) {
            postMessage({ 'id': id, 'error': { 'message': "Invalid database ID" } });
            return;
        }
        const statement = database.prepare(requestData.sql);
        statements.set(newStatementId, statement);

        const resultData = {
            'statementId': newStatementId,
            'parameterCount': statement.parameterCount,
            'columnNames': statement.columnNames
        };

        postMessage({ 'id': id, 'result': resultData });
    } catch (error) {
        postMessage({ 'id': id, 'error': { 'message': error.message } });
    }
}

function bindRequest(id, requestData) {
    try {
        const statement = statements.get(requestData.statementId);
        if (!statement) {
            postMessage({ 'id': id, 'error': { 'message': "Invalid statement ID" } });
            return;
        }
        statement.reset();
        statement.clearBindings();
        const bindings = requestData.bindings || [];
        for (let i = 0; i < bindings.length; i++) {
            statement.bind(i + 1, bindings[i]);
        }
        postMessage({ 'id': id, 'result': {} });
    } catch (error) {
        postMessage({ 'id': id, 'error': { 'message': error.message } });
    }
}

function stepRequest(id, requestData) {
    try {
        const statement = statements.get(requestData.statementId);
        if (!statement) {
            postMessage({ 'id': id, 'error': { 'message': "Invalid statement ID" } });
            return;
        }

        const hasRow = statement.step();
        if (hasRow) {
            const row = statement.get([]);
            const columnTypes = [];
            for (let i = 0; i < statement.columnCount; i++) {
                columnTypes.push(sqlite3.capi.sqlite3_column_type(statement.pointer, i));
            }
            postMessage({
                'id': id,
                'result': { 'hasRow': true, 'row': row, 'columnTypes': columnTypes }
            });
        } else {
            postMessage({ 'id': id, 'result': { 'hasRow': false } });
        }
    } catch (error) {
        postMessage({ 'id': id, 'error': { 'message': error.message } });
    }
}

function closeRequest(id, requestData) {
    try {
        if (requestData.statementId !== undefined) {
            const statement = statements.get(requestData.statementId);
            if (statement) {
                statement.finalize();
                statements.delete(requestData.statementId);
            }
        } else if (requestData.databaseId !== undefined) {
            const database = databases.get(requestData.databaseId);
            if (database) {
                database.close();
                databases.delete(requestData.databaseId);
            }
        }
        postMessage({ 'id': id, 'result': {} });
    } catch (error) {
        postMessage({ 'id': id, 'error': { 'message': error.message } });
    }
}

const commandMap = {
    'open': openRequest,
    'prepare': prepareRequest,
    'bind': bindRequest,
    'step': stepRequest,
    'close': closeRequest,
};

function handleMessage(e) {
    const requestMsg = e.data;
    if (!requestMsg || typeof requestMsg.id === 'undefined') return;

    const id = requestMsg.id;
    const type = requestMsg.type;
    const requestHandler = commandMap[type];

    if (requestHandler) {
        requestHandler(id, requestMsg);
    } else {
        postMessage({
            'id': id,
            'error': { 'message': "Unknown command: " + type }
        });
    }
}

const messageQueue = [];
onmessage = (e) => {
    if (!sqlite3) {
        messageQueue.push(e);
    } else {
        handleMessage(e);
    }
};

sqlite3InitModule().then(instance => {
    sqlite3 = instance;
    while (messageQueue.length > 0) {
        handleMessage(messageQueue.shift());
    }
});
