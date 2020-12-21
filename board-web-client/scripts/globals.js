const UPDATE_INTERVAL = 1000;

const canvas = document.getElementById('canvas');
const ctx = canvas.getContext('2d');
const clients = new Map();
const inviteButton = document.getElementById('inviteButton');

var localClient;
