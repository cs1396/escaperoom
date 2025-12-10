package group24.escaperoom.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import group24.escaperoom.engine.BackManager;
import group24.escaperoom.screens.utils.ScreenManager;
import group24.escaperoom.services.Networking.StatusCode;
import group24.escaperoom.services.User;
import group24.escaperoom.ui.notifications.Notifier;
import group24.escaperoom.ui.widgets.G24TextButton;
import group24.escaperoom.ui.widgets.G24TextInput;

public class Login extends MenuScreen {

  G24TextInput userNameField;
  G24TextInput passwordField;
  G24TextInput confirmPasswordField;
  int FIELD_WIDTH = 300;

  String username;
  String password;

  private class CreateAccountButton extends G24TextButton {
    public CreateAccountButton() {
      super("Create Account");
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
        rootTable.add(CreateAccountButton.this).colspan(2);

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
      super("Login");
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
    userNameField = new G24TextInput("", skin);
    userNameField.setAlphanumeric();
    userNameField.setOnEnter(() -> {
      if (loginButton.getStage() != null) loginButton.tryLogin();
      else createAccountButton.tryCreateAccount();
    });

    passwordField = new G24TextInput("", skin);
    passwordField.setPasswordMode(true);
    passwordField.setOnEnter(() -> {
      if (loginButton.getStage() != null) loginButton.tryLogin();
      else createAccountButton.tryCreateAccount();
    });

    confirmPasswordField = new G24TextInput("", skin);
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
    rootTable.add(loginButton).colspan(2);
    rootTable.row();
    rootTable.add(createAccountButton).colspan(2);
  }
}
