package cs1396.escaperoom.ui;


import cs1396.escaperoom.ui.widgets.G24TextButton;

import cs1396.escaperoom.ui.widgets.G24Dialog;

public class ConfirmDialog extends G24Dialog {

  public static class Builder extends G24Dialog.AbstractBuilder<ConfirmDialog, Builder> {
    public CloseHandler onConfirm =  () -> true;
    public CloseHandler onCancel = () -> true;
    public String confirmText = "Confirm";
    public String cancelText = "Cancel";

    public Builder self(){ return this; }
    public ConfirmDialog build(){ return new ConfirmDialog(this); }

    public Builder(String title){
      super(title);
    }

    /**
     * Register a {@link CloseHandler} for when the dialog is closed 
     * by the confirm button
     */
    public Builder onConfirm(CloseHandler handler){
      onConfirm = handler;
      return this;
    }

    /**
     * Register a {@link CloseHandler} for when the dialog is closed 
     * by the cancel button
     */
    public Builder onCancel(CloseHandler handler){
      onCancel = handler;
      return this;
    }

    /**
     * Set the text on the confirm button to {@code msg}
     */
    public Builder confirmText(String msg){
      confirmText = msg;
      return this;
    }

    /**
     * Set the text on the cancel button to {@code msg}
     */
    public Builder cancelText(String msg){
      cancelText = msg;
      return this;
    }
  }

  protected ConfirmDialog(Builder builder){
    super(builder);

    G24TextButton confirmButton = new G24TextButton(builder.confirmText);
    G24TextButton cancelButton = new G24TextButton(builder.cancelText);

    confirmButton.autoResetCheck();
    cancelButton.autoResetCheck();

    button(confirmButton, builder.onConfirm);
    button(cancelButton, builder.onCancel);
  }
}
