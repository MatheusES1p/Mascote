import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class Mascote {

    static final int TAMANHO = 200;

    enum Estado { PARADO, PENSANDO }

    // ---------- Painel que desenha o mascote ----------
    // Para colocar sprite/animação depois: mexa só em paintComponent
    // (e, se quiser animar, use um javax.swing.Timer chamando repaint()).
    static class MascotePanel extends JPanel {
        private Estado estado = Estado.PARADO;

        MascotePanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(TAMANHO, TAMANHO));
        }

        void setEstado(Estado novo) {
            this.estado = novo;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            // Limpa o fundo com transparência total (evita "rastros" na janela transparente)
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // ---- VISUAL PROVISÓRIO ----
            Color cor = (estado == Estado.PARADO) ? new Color(200, 64, 255) : new Color(64, 112, 255);
            int d = 120;
            int x = (getWidth() - d) / 2;
            int y = getHeight() - d;
            g2.setColor(cor);
            g2.fillOval(x, y, d, d);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Mascote::iniciar);
    }

    static void iniciar() {
        // JWindow: sem borda e sem botão na barra de tarefas
        JWindow janela = new JWindow();
        janela.setBackground(new Color(0, 0, 0, 0)); // transparente
        janela.setAlwaysOnTop(true);

        MascotePanel painel = new MascotePanel();
        janela.setContentPane(painel);
        janela.pack();

        // Canto inferior direito da área útil (já descontando a barra de tarefas)
        Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        janela.setLocation(area.x + area.width - TAMANHO - 40, area.y + area.height - TAMANHO);

        // ---------- Menu de clique direito ----------
        JPopupMenu menu = new JPopupMenu();
        JMenuItem esconder = new JMenuItem("Esconder");
        esconder.addActionListener(e -> janela.setVisible(false));
        JMenu estados = new JMenu("Estado (teste)");
        JMenuItem parado = new JMenuItem("Parado");
        parado.addActionListener(e -> painel.setEstado(Estado.PARADO));
        JMenuItem pensando = new JMenuItem("Pensando");
        pensando.addActionListener(e -> painel.setEstado(Estado.PENSANDO));
        estados.add(parado);
        estados.add(pensando);
        JMenuItem sair = new JMenuItem("Sair");
        sair.addActionListener(e -> System.exit(0));
        menu.add(esconder);
        menu.add(estados);
        menu.addSeparator();
        menu.add(sair);

        // ---------- Arrastar + clique direito ----------
        MouseAdapter mouse = new MouseAdapter() {
            private Point offset;

            @Override public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) { menu.show(painel, e.getX(), e.getY()); return; }
                offset = e.getPoint();
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) menu.show(painel, e.getX(), e.getY());
                offset = null;
            }
            @Override public void mouseDragged(MouseEvent e) {
                if (offset == null) return;
                Point p = e.getLocationOnScreen();
                janela.setLocation(p.x - offset.x, p.y - offset.y);
            }
        };
        painel.addMouseListener(mouse);
        painel.addMouseMotionListener(mouse);

        // ---------- Bandeja do sistema ----------
        if (SystemTray.isSupported()) {
            BufferedImage icone = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = icone.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(200, 64, 255));
            g.fillOval(1, 1, 14, 14);
            g.dispose();

            PopupMenu trayMenu = new PopupMenu();
            MenuItem mostrar = new MenuItem("Mostrar");
            mostrar.addActionListener(e -> janela.setVisible(true));
            MenuItem esconderTray = new MenuItem("Esconder");
            esconderTray.addActionListener(e -> janela.setVisible(false));
            MenuItem sairTray = new MenuItem("Sair");
            sairTray.addActionListener(e -> System.exit(0));
            trayMenu.add(mostrar);
            trayMenu.add(esconderTray);
            trayMenu.addSeparator();
            trayMenu.add(sairTray);

            TrayIcon trayIcon = new TrayIcon(icone, "Mascote", trayMenu);
            trayIcon.addActionListener(e -> janela.setVisible(!janela.isVisible())); // duplo clique
            try {
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        }

        janela.setVisible(true);
    }
}
