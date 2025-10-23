package spotidal;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.io.IOException;

public class SpotidalTUI {
    private final ServiceRegistry serviceRegistry;

    public SpotidalTUI(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void start() throws IOException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Screen screen = terminalFactory.createScreen();
        screen.startScreen();
        WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
        Window window = new BasicWindow("spotidal.Spotidal - Spotify zu Tidal Migration");

        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(1));
        panel.addComponent(new Label("Bitte wähle eine Aktion:"));
        panel.addComponent(new Button("Authentifizierung", () -> {
            showAuthSubMenu(textGUI);
        }));

        panel.addComponent(new Button("Beenden", window::close));
        window.setComponent(panel);
        textGUI.addWindowAndWait(window);
        screen.stopScreen();
    }

    private void showAuthSubMenu(WindowBasedTextGUI textGUI) {
        Window authWindow = new BasicWindow("Authentifizierung wählen");
        Panel authPanel = new Panel();
        authPanel.setLayoutManager(new GridLayout(1));
        authPanel.addComponent(new Label("Welchen Dienst möchtest du authentifizieren?"));
        authPanel.addComponent(new Button("Spotify", () -> {
            var token = serviceRegistry.getSpotifyAuthService().authenticate();
            MessageDialog.showMessageDialog(textGUI, "Info", "Spotify-Authentifizierung abgeschlossen.");
            token.ifPresentOrElse(
                _ -> MessageDialog.showMessageDialog(textGUI, "Info", "Spotify-Authentifizierung abgeschlossen."),
                () -> MessageDialog.showMessageDialog(textGUI, "Fehler", "Spotify-Authentifizierung fehlgeschlagen.")
            );
        }));
        authPanel.addComponent(new Button("Tidal", () -> {
            var token = serviceRegistry.getTidalAuthService().authenticate();
            token.ifPresentOrElse(
                _ -> MessageDialog.showMessageDialog(textGUI, "Info", "Tidal-Authentifizierung abgeschlossen."),
                () -> MessageDialog.showMessageDialog(textGUI, "Fehler", "Tidal-Authentifizierung fehlgeschlagen.")
            );
        }));
        authPanel.addComponent(new Button("Zurück", authWindow::close));
        authWindow.setComponent(authPanel);
        textGUI.addWindowAndWait(authWindow);
    }

}
