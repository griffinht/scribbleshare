const { app, BrowserWindow } = require('electron')
const path = require('path')
const url = require('url')

let window;

function createWindow() {
    window = new BrowserWindow({
        icon: 'favicon.ico',
        title: 'scribbleshare',
        show: false,
    });
    window.removeMenu();
    window.once('ready-to-show', () => {
        window.maximize();
        window.show()
    })
    window.loadURL('http://scribbleshare.com/').then(() => {

    });
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