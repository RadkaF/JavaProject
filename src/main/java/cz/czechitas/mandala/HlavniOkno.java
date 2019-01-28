package cz.czechitas.mandala;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.*;
import net.sevecek.util.ApplicationPublicException;

public class HlavniOkno extends JFrame {

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JLabel labelSoubor;
    private JButton btnUlozit;
    private JButton btnNahrat;
    private JLabel labelAktualniBarva;
    private JLabel labBarva1;
    private JLabel labBarva2;
    private JLabel labBarva3;
    private JLabel labBarva4;
    private JLabel labObrazek;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
    JPanel contentPane;
    MigLayout migLayoutManager;
    BufferedImage obrazek;
    Color pouzivanaBarva;
    File otevrenySoubor;

    public HlavniOkno() {
        try {
            initComponents();
            File soubor = new File("/Users/radka/Java-Training/Projects/Java1/Lekce11/Ukol10-Mandala/obrazek1.png");
            obrazek = ImageIO.read(soubor);
            labObrazek.setIcon(new ImageIcon(obrazek));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onMousePress(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        vyplnObrazek(obrazek, x, y, changeColor());
        labObrazek.repaint();
    }

    private Color changeColor() {
        return pouzivanaBarva;
    }

    public void vyplnObrazek(BufferedImage obrazek, int x, int y, Color barva) {
        if (barva == null) {
            barva = new Color(255, 255, 255);
        }

        // Zamez vyplnovani mimo rozsah
        if (x < 0 || x >= obrazek.getWidth() || y < 0 || y >= obrazek.getHeight()) {
            return;
        }

        WritableRaster pixely = obrazek.getRaster();
        int[] novyPixel = new int[] {barva.getRed(), barva.getGreen(), barva.getBlue(), barva.getAlpha()};
        int[] staryPixel = new int[] {255, 255, 255, 255};
        staryPixel = pixely.getPixel(x, y, staryPixel);

        // Pokud uz je pocatecni pixel obarven na cilovou barvu, nic nemen
        if (pixelyMajiStejnouBarvu(novyPixel, staryPixel)) {
            return;
        }

        // Zamez prebarveni cerne cary
        int[] cernyPixel = new int[] {0, 0, 0, 0};
        if (pixelyMajiStejnouBarvu(cernyPixel, staryPixel)) {
            return;
        }

        vyplnovaciSmycka(pixely, x, y, novyPixel, staryPixel);
    }

    /**
     * Provede skutecne vyplneni pomoci zasobniku
     */
    private void vyplnovaciSmycka(WritableRaster raster, int x, int y, int[] novaBarva, int[] nahrazovanaBarva) {
        Rectangle rozmery = raster.getBounds();
        int[] aktualniBarva = new int[] {255, 255, 255, 255};

        Deque<Point> zasobnik = new ArrayDeque<>(rozmery.width * rozmery.height);
        zasobnik.push(new Point(x, y));
        while (zasobnik.size() > 0) {
            Point point = zasobnik.pop();
            x = point.x;
            y = point.y;
            if (!pixelyMajiStejnouBarvu(raster.getPixel(x, y, aktualniBarva), nahrazovanaBarva)) {
                continue;
            }

            // Najdi levou zed, po ceste vyplnuj
            int levaZed = x;
            do {
                raster.setPixel(levaZed, y, novaBarva);
                levaZed--;
            }
            while (levaZed >= 0 && pixelyMajiStejnouBarvu(raster.getPixel(levaZed, y, aktualniBarva), nahrazovanaBarva));
            levaZed++;

            // Najdi pravou zed, po ceste vyplnuj
            int pravaZed = x;
            do {
                raster.setPixel(pravaZed, y, novaBarva);
                pravaZed++;
            }
            while (pravaZed < rozmery.width && pixelyMajiStejnouBarvu(raster.getPixel(pravaZed, y, aktualniBarva), nahrazovanaBarva));
            pravaZed--;

            // Pridej na zasobnik body nahore a dole
            for (int i = levaZed; i <= pravaZed; i++) {
                if (y > 0 && pixelyMajiStejnouBarvu(raster.getPixel(i, y - 1, aktualniBarva), nahrazovanaBarva)) {
                    if (!(i > levaZed && i < pravaZed
                            && pixelyMajiStejnouBarvu(raster.getPixel(i - 1, y - 1, aktualniBarva), nahrazovanaBarva)
                            && pixelyMajiStejnouBarvu(raster.getPixel(i + 1, y - 1, aktualniBarva), nahrazovanaBarva))) {
                        zasobnik.add(new Point(i, y - 1));
                    }
                }
                if (y < rozmery.height - 1 && pixelyMajiStejnouBarvu(raster.getPixel(i, y + 1, aktualniBarva), nahrazovanaBarva)) {
                    if (!(i > levaZed && i < pravaZed
                            && pixelyMajiStejnouBarvu(raster.getPixel(i - 1, y + 1, aktualniBarva), nahrazovanaBarva)
                            && pixelyMajiStejnouBarvu(raster.getPixel(i + 1, y + 1, aktualniBarva), nahrazovanaBarva))) {
                        zasobnik.add(new Point(i, y + 1));
                    }
                }
            }
        }
    }

    /**
     * Vrati true pokud RGB hodnoty v polich jsou stejne. Pokud jsou ruzne, vraci false
     */
    private boolean pixelyMajiStejnouBarvu(int[] barva1, int[] barva2) {
        return barva1[0] == barva2[0] && barva1[1] == barva2[1] && barva1[2] == barva2[2];
    }

    private void labBarva1ZmenaBarvy(MouseEvent e) {
        Color barva1 = new Color(255, 255, 153);
        pouzivanaBarva = barva1;
        if (pouzivanaBarva.getRGB() == barva1.getRGB()) {
            labBarva1.setText("X");
            labBarva2.setText("");
            labBarva3.setText("");
            labBarva4.setText("");
        }
    }

    private void labBarva2ZmenaBarvy(MouseEvent e) {
        Color barva2 = new Color(0, 204, 204);
        pouzivanaBarva = barva2;
        if (pouzivanaBarva.getRGB() == barva2.getRGB()) {
            labBarva1.setText("");
            labBarva2.setText("X");
            labBarva3.setText("");
            labBarva4.setText("");
        }

    }

    private void labBarva3ZmenaBarvy(MouseEvent e) {
        Color barva3 = new Color(204, 204, 255);
        pouzivanaBarva = barva3;
        if (pouzivanaBarva.getRGB() == barva3.getRGB()) {
            labBarva1.setText("");
            labBarva2.setText("");
            labBarva3.setText("X");
            labBarva4.setText("");
        }
    }

    private void labBarva4ZmenaBarvy(MouseEvent e) {
        Color barva4 = new Color(255, 153, 153);
        pouzivanaBarva = barva4;
        if (pouzivanaBarva.getRGB() == barva4.getRGB()) {
            labBarva1.setText("");
            labBarva2.setText("");
            labBarva3.setText("");
            labBarva4.setText("X");
        }
    }

    private void ulozitJako(MouseEvent e) {
        JFileChooser dialog;
        dialog = new JFileChooser(".");

        int vysledek = dialog.showSaveDialog(this);
        if (vysledek != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File soubor = dialog.getSelectedFile();
        if (!soubor.getName().contains(".") && !soubor.exists()) {
            soubor = new File(soubor.getParentFile(), soubor.getName() + ".png");
        }
        if (soubor.exists()) {
            int potvrzeni = JOptionPane.showConfirmDialog(this, "Soubor " + soubor.getName() + " už existuje.\nChcete jej přepsat?", "Přepsat soubor?", JOptionPane.YES_NO_OPTION);
            if (potvrzeni == JOptionPane.NO_OPTION) {
                return;
            }
        }
        ulozObrazek(soubor);
    }

    private void ulozObrazek(File soubor) {
        try {
            ImageIO.write(obrazek, "png", soubor);
        } catch (IOException ex) {
            throw new ApplicationPublicException(ex, "Nepodařilo se uložit obrázek mandaly do souboru " + soubor.getAbsolutePath());
        }
    }

    private void otevriMenu(MouseEvent e) {
        JFileChooser dialog;
        if (otevrenySoubor == null) {
            dialog = new JFileChooser(".");
        } else {
            dialog = new JFileChooser(otevrenySoubor.getParentFile());
        }
        dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        dialog.setMultiSelectionEnabled(false);
        dialog.addChoosableFileFilter(new FileNameExtensionFilter("Obrázky (*.png)", "png"));
        int vysledek = dialog.showOpenDialog(this);
        if (vysledek != JFileChooser.APPROVE_OPTION) {
            return;
        }

        otevrenySoubor = dialog.getSelectedFile();
        nahrajObrazek(otevrenySoubor);
    }


    private void nahrajObrazek(File soubor) {
        if (soubor == null) {
            try {
                obrazek = ImageIO.read(getClass().getResourceAsStream("/cz/czechitas/mandala/mandala.png"));
            } catch (IOException ex) {
                throw new ApplicationPublicException(ex, "Nepodařilo se nahrát zabudovaný obrázek mandaly");
            }
        } else {
            try {
                obrazek = ImageIO.read(soubor);
            } catch (IOException ex) {
                throw new ApplicationPublicException(ex, "Nepodařilo se nahrát obrázek mandaly ze souboru " + soubor.getAbsolutePath());
            }
        }
        labObrazek.setIcon(new ImageIcon(obrazek));
        labObrazek.setMinimumSize(new Dimension(obrazek.getWidth(), obrazek.getHeight()));
        pack();
        setMinimumSize(getSize());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        labelSoubor = new JLabel();
        btnUlozit = new JButton();
        btnNahrat = new JButton();
        labelAktualniBarva = new JLabel();
        labBarva1 = new JLabel();
        labBarva2 = new JLabel();
        labBarva3 = new JLabel();
        labBarva4 = new JLabel();
        labObrazek = new JLabel();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Mandala");
        Container contentPane = getContentPane();
        contentPane.setLayout(new MigLayout(
            "insets rel,hidemode 3",
            // columns
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[grow,fill]",
            // rows
            "[]" +
            "[32]" +
            "[grow,fill]"));
        this.contentPane = (JPanel) this.getContentPane();
        this.contentPane.setBackground(this.getBackground());
        LayoutManager layout = this.contentPane.getLayout();
        if (layout instanceof MigLayout) {
            this.migLayoutManager = (MigLayout) layout;
        }

        //---- labelSoubor ----
        labelSoubor.setText("Soubor");
        contentPane.add(labelSoubor, "cell 0 0");

        //---- btnUlozit ----
        btnUlozit.setText("Ulo\u017eit jako");
        btnUlozit.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ulozitJako(e);
            }
        });
        contentPane.add(btnUlozit, "cell 1 0 3 1,alignx center,growx 0");

        //---- btnNahrat ----
        btnNahrat.setText("Nahr\u00e1t");
        btnNahrat.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                otevriMenu(e);
            }
        });
        contentPane.add(btnNahrat, "cell 4 0 4 1,growx");

        //---- labelAktualniBarva ----
        labelAktualniBarva.setText("Aktu\u00e1ln\u00ed barva:");
        contentPane.add(labelAktualniBarva, "cell 0 1,alignx left,growx 0");

        //---- labBarva1 ----
        labBarva1.setOpaque(true);
        labBarva1.setBackground(new Color(255, 255, 153));
        labBarva1.setPreferredSize(new Dimension(32, 32));
        labBarva1.setForeground(Color.gray);
        labBarva1.setHorizontalAlignment(SwingConstants.CENTER);
        labBarva1.setFont(new Font(".SF NS Text", Font.PLAIN, 20));
        labBarva1.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                labBarva1ZmenaBarvy(e);
            }
        });
        contentPane.add(labBarva1, "cell 0 1,alignx label,growx 0");

        //---- labBarva2 ----
        labBarva2.setBackground(new Color(0, 204, 204));
        labBarva2.setOpaque(true);
        labBarva2.setPreferredSize(new Dimension(32, 32));
        labBarva2.setForeground(Color.gray);
        labBarva2.setHorizontalAlignment(SwingConstants.CENTER);
        labBarva2.setFont(new Font(".SF NS Text", Font.PLAIN, 20));
        labBarva2.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                labBarva2ZmenaBarvy(e);
            }
        });
        contentPane.add(labBarva2, "cell 0 1,alignx label,growx 0");

        //---- labBarva3 ----
        labBarva3.setOpaque(true);
        labBarva3.setBackground(new Color(204, 204, 255));
        labBarva3.setPreferredSize(new Dimension(32, 32));
        labBarva3.setForeground(Color.gray);
        labBarva3.setHorizontalAlignment(SwingConstants.CENTER);
        labBarva3.setFont(new Font(".SF NS Text", Font.PLAIN, 20));
        labBarva3.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                labBarva3ZmenaBarvy(e);
            }
        });
        contentPane.add(labBarva3, "cell 0 1,alignx label,growx 0");

        //---- labBarva4 ----
        labBarva4.setBackground(new Color(255, 153, 153));
        labBarva4.setOpaque(true);
        labBarva4.setPreferredSize(new Dimension(32, 32));
        labBarva4.setForeground(Color.gray);
        labBarva4.setHorizontalAlignment(SwingConstants.CENTER);
        labBarva4.setFont(new Font(".SF NS Text", Font.PLAIN, 20));
        labBarva4.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                labBarva4ZmenaBarvy(e);
            }
        });
        contentPane.add(labBarva4, "cell 0 1,alignx label,growx 0");

        //---- labObrazek ----
        labObrazek.setBackground(Color.white);
        labObrazek.setOpaque(true);
        labObrazek.setHorizontalAlignment(SwingConstants.LEFT);
        labObrazek.setVerticalAlignment(SwingConstants.TOP);
        labObrazek.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onMousePress(e);
            }
        });
        contentPane.add(labObrazek, "cell 0 2 10 1,wmin 32,hmin 32");
        setSize(660, 755);
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
}
