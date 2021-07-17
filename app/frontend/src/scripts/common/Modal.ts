export default class Modal {
    modal: HTMLElement;

    constructor(modal: HTMLElement) {
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