const { app, BrowserWindow } = require('electron')
const path = require('path')
const url = require('url')

let window;

function createWindow() {
    window = new BrowserWindow({
        width: 800,
        height: 800,
        fullscreen: true,
    });
    window.loadURL('http://scribbleshare.com/');
}

app.whenReady().then(() => {
    createWindow();

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) {
            createWindow();
        }
    });
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit()
    }
})