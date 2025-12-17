package cs1396.escaperoom.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.screens.utils.ScreenManager;
import cs1396.escaperoom.services.Networking.StatusCode;
import cs1396.escaperoom.services.User;
import cs1396.escaperoom.ui.notifications.Notifier;
import cs1396.escaperoom.ui.widgets.G24TextButton;
import cs1396.escaperoom.ui.widgets.G24TextInput;

public class Login extends MenuScreen {

  G24TextInput userNameField;
  G24TextInput passwordField;
  G24TextInput confirmPasswordField;
  int FIELD_WIDTH = 300;

  String username;
  String password;

  private class CreateAccountButton extends G24TextButton {
    public CreateAccountButton() {
      super("Create Account", "med-text");

      setProgrammaticChangeEvents(false);
      this.addListener(switchToCreateAccount);
    }

    ChangeListener switchToCreateAccount = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        CreateAccountButton.this.setChecked(false);
        rootTable.clear();
        rootTable.add(new Label("USERNAME", skin, "bubble"));
        rootTable.add(userNameField).width(300);
        rootTable.row();
        rootTable.add(new Label("PASSWORD", skin, "bubble"));
        rootTable.add(passwordField).width(300);
        rootTable.row();
        rootTable.add(new Label("CONFIRM PASSWORD", skin, "bubble"));
        rootTable.add(confirmPasswordField).width(300);
        rootTable.row();
        rootTable.add(CreateAccountButton.this).colspan(2).minWidth(300);

        CreateAccountButton.this.removeListener(switchToCreateAccount);
        CreateAccountButton.this.addListener(tryAccountCreation);
      }
    };

    void tryCreateAccount() {
      CreateAccountButton.this.setChecked(false);

      if (!passwordField.getText().equals(confirmPasswordField.getText())) {
        Notifier.error("Passwords do not match");
        return;
      }

      waitFor(User.createAccount(userNameField.getText(), passwordField.getText()), (StatusCode code) -> {
        if (User.isLoggedIn()) {
          ScreenManager.instance().showScreen(new OnlineMainMenu());
        } else {
          Notifier.error("Failed to create account: (code: " + code.name() + ")");
        }
      }, "Creating account and logging in");
    }

    ChangeListener tryAccountCreation = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (!CreateAccountButton.this.isChecked())
          return;

        tryCreateAccount();
      }
    };
  }

  private class LoginButton extends G24TextButton {
    public LoginButton() {
      super("Login", "med-text");
      setProgrammaticChangeEvents(false);
      addListener(tryLoginListener);
    }

    ChangeListener tryLoginListener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (!LoginButton.this.isChecked())
          return;

        tryLogin();
      }
    };

    void tryLogin() {
      this.setChecked(false);
      waitFor(User.AttemptLogin(userNameField.getText(), passwordField.getText()), (StatusCode code) -> {
        if (User.isLoggedIn() && code == StatusCode.OK) {
          ScreenManager.instance().showScreen(new OnlineMainMenu());
        } else {
          Notifier.warn("Error logging in: " + code.toString());
        }
      });
    }
  }

  LoginButton loginButton;
  CreateAccountButton createAccountButton;

  TextButton backButton;

  ChangeListener onBackButton = new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
      ScreenManager.instance().showScreen(new MainMenu());
    }
  };

  @Override
  public void init() {
    userNameField = new G24TextInput("");
    userNameField.setAlphanumeric();
    userNameField.setOnEnter(() -> {
      if (loginButton.getStage() != null) loginButton.tryLogin();
      else createAccountButton.tryCreateAccount();
    });

    passwordField = new G24TextInput("");
    passwordField.setPasswordMode(true);
    passwordField.setOnEnter(() -> {
      if (loginButton.getStage() != null) loginButton.tryLogin();
      else createAccountButton.tryCreateAccount();
    });

    confirmPasswordField = new G24TextInput("");
    confirmPasswordField.setPasswordMode(true);
    confirmPasswordField.setOnEnter(() -> {
      if (loginButton.getStage() != null) loginButton.tryLogin();
      else createAccountButton.tryCreateAccount();
    });

    backButton = new G24TextButton("<-");
    backButton.addListener(onBackButton);

    loginButton = new LoginButton();
    createAccountButton = new CreateAccountButton();

    buildUI();
    BackManager.addBack(() -> {
      ScreenManager.instance().showScreen(new MainMenu());
    });
  }

  private void buildUI() {
    addUI(backButton);

    backButton.setPosition(0 + backButton.getWidth() + 5, getUIStage().getHeight() - backButton.getHeight() - 30);

    rootTable.defaults().pad(10);
    rootTable.setFillParent(true);

    passwordField.setMaxLength(FIELD_WIDTH);
    userNameField.setMaxLength(FIELD_WIDTH);

    rootTable.add(new Label("USERNAME:", skin, "bubble"));
    rootTable.add(userNameField).width(FIELD_WIDTH);
    rootTable.row();
    rootTable.add(new Label("PASSWORD:", skin, "bubble"));
    rootTable.add(passwordField).width(FIELD_WIDTH);
    rootTable.row();
    rootTable.add(loginButton).colspan(2).minWidth(300);
    rootTable.row();
    rootTable.add(createAccountButton).colspan(2).minWidth(300);
  }
}
