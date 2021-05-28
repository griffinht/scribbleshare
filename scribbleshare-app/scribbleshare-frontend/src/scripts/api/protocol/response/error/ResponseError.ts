export default class HttpError {
    responseCode: number;

    constructor(responseCode: number) {
        this.responseCode = responseCode;
    }
}