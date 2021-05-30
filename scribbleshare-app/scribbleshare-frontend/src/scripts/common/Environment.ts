interface Environment {
    getApiHost(): string;
    getWebsocketHost(): string;
}

class DevEnvironment implements Environment {
    getApiHost(): string {
        return 'http://localhost:3456';
    }

    getWebsocketHost(): string {
        return 'ws://localhost:18080/scribblesocket';
    }
}

class ProductionEnvironment implements Environment {
    getApiHost(): string {
        return 'http://scribbleshare.com';
    }

    getWebsocketHost(): string {
        return 'ws://scribbleshare.com:8080/scribblesocket';
    }
}

let environment: Environment;
if (window.location.href.startsWith("http://localhost:3456/")) {//todo this is probably fine
    console.warn('Development environment detected');
    environment = new DevEnvironment();
} else {
    environment = new ProductionEnvironment();
}

export default environment;