export default class Modal {
    constructor(modal) {
        this.modal = modal;
        this.hide();
    }

    show() {
        this.modal.style.visibility = 'visible';
    }

    hide() {
        this.modal.style.visibility = 'hidden';
    }
}