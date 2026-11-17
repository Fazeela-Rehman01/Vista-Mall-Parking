import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

// ============================================================================
// 1. ABSTRACT CLASS: VEHICLE
// ============================================================================
abstract class Vehicle {
    private String licensePlate;
    private String ownerName;
    private String phoneNumber;
    private String model;
    private String color;
    private long entryTime;

    public Vehicle(String licensePlate, String ownerName, String phoneNumber, String model, String color) {
        this.licensePlate = licensePlate.toUpperCase().trim();
        this.ownerName = ownerName.trim();
        this.phoneNumber = phoneNumber.trim();
        this.model = (model == null || model.trim().isEmpty()) ? "-" : model.trim();
        this.color = color.trim();
        this.entryTime = System.currentTimeMillis();
    }

    public Vehicle(String licensePlate, String ownerName, String phoneNumber, String model, String color, long entryTime) {
        this.licensePlate = licensePlate.toUpperCase().trim();
        this.ownerName = ownerName.trim();
        this.phoneNumber = phoneNumber.trim();
        this.model = (model == null || model.trim().isEmpty()) ? "-" : model.trim();
        this.color = color.trim();
        this.entryTime = entryTime;
    }

    public String getLicensePlate() { return licensePlate; }
    public String getOwnerName() { return ownerName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getModel() { return model; }
    public String getColor() { return color; }
    public long getEntryTime() { return entryTime; }

    public String getFormattedEntryTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        return sdf.format(new Date(entryTime));
    }

    public abstract double getHourlyRate();
    public abstract double calculateFee(long hours);
    public abstract String getVehicleType();
}

// ============================================================================
// 2. SUBCLASSES (POLYMORPHIC INHERITANCE)
// ============================================================================
class Bike extends Vehicle {
    private static final double HOURLY_RATE = 20.0;

    public Bike(String licensePlate, String ownerName, String phoneNumber, String model, String color) {
        super(licensePlate, ownerName, phoneNumber, model, color);
    }
    public Bike(String licensePlate, String ownerName, String phoneNumber, String model, String color, long entryTime) {
        super(licensePlate, ownerName, phoneNumber, model, color, entryTime);
    }

    @Override public double getHourlyRate() { return HOURLY_RATE; }
    @Override public double calculateFee(long hours) { return hours * HOURLY_RATE; }
    @Override public String getVehicleType() { return "Bike"; }
}

class Rickshaw extends Vehicle {
    private static final double HOURLY_RATE = 30.0;

    public Rickshaw(String licensePlate, String ownerName, String phoneNumber, String color) {
        super(licensePlate, ownerName, phoneNumber, "-", color);
    }
    public Rickshaw(String licensePlate, String ownerName, String phoneNumber, String color, long entryTime) {
        super(licensePlate, ownerName, phoneNumber, "-", color, entryTime);
    }

    @Override public double getHourlyRate() { return HOURLY_RATE; }
    @Override public double calculateFee(long hours) { return hours * HOURLY_RATE; }
    @Override public String getVehicleType() { return "Rickshaw"; }
}

class Car extends Vehicle {
    private static final double HOURLY_RATE = 50.0;

    public Car(String licensePlate, String ownerName, String phoneNumber, String model, String color) {
        super(licensePlate, ownerName, phoneNumber, model, color);
    }
    public Car(String licensePlate, String ownerName, String phoneNumber, String model, String color, long entryTime) {
        super(licensePlate, ownerName, phoneNumber, model, color, entryTime);
    }

    @Override public double getHourlyRate() { return HOURLY_RATE; }
    @Override public double calculateFee(long hours) { return hours * HOURLY_RATE; }
    @Override public String getVehicleType() { return "Car"; }
}

// ============================================================================
// 3. CUSTOM EXCEPTION CLASSES
// ============================================================================
class SlotUnavailableException extends Exception { public SlotUnavailableException(String msg) { super(msg); } }
class VehicleNotFoundException extends Exception { public VehicleNotFoundException(String msg) { super(msg); } }
class InvalidModelException extends Exception { public InvalidModelException(String msg) { super(msg); } }
class InvalidColorException extends Exception { public InvalidColorException(String msg) { super(msg); } }
class InvalidNameException extends Exception { public InvalidNameException(String msg) { super(msg); } }
class DuplicateEntryException extends Exception { public DuplicateEntryException(String msg) { super(msg); } }

// ============================================================================
// 4. PARKING SLOT CLASS
// ============================================================================
class ParkingSlot {
    private int slotId;
    private String supportedType;
    private boolean isOccupied;
    private Vehicle parkedVehicle;

    public ParkingSlot(int slotId, String supportedType) {
        this.slotId = slotId;
        this.supportedType = supportedType;
        this.isOccupied = false;
        this.parkedVehicle = null;
    }

    public int getSlotId() { return slotId; }
    public String getSupportedType() { return supportedType; }
    public boolean isOccupied() { return isOccupied; }
    public Vehicle getParkedVehicle() { return parkedVehicle; }

    public void parkVehicle(Vehicle v) {
        this.parkedVehicle = v;
        this.isOccupied = true;
    }

    public void vacateSlot() {
        this.parkedVehicle = null;
        this.isOccupied = false;
    }
}

// ============================================================================
// 5. PARKING MANAGER
// ============================================================================
class ParkingLotManager {
    private List<ParkingSlot> slots;
    private final String LOG_FILE = "parking_records.txt";
    private final String ACTIVE_FILE = "active_parking.txt";

    public ParkingLotManager() {
        slots = new ArrayList<>();
        int id = 1;
        for (int i = 0; i < 50; i++) slots.add(new ParkingSlot(id++, "Bike"));
        for (int i = 0; i < 50; i++) slots.add(new ParkingSlot(id++, "Car"));
        for (int i = 0; i < 30; i++) slots.add(new ParkingSlot(id++, "Rickshaw"));

        loadActiveParkingFromFile();
    }

    public List<ParkingSlot> getSlots() { return slots; }

    public int parkVehicle(Vehicle vehicle) throws SlotUnavailableException, DuplicateEntryException {
        for (ParkingSlot slot : slots) {
            if (slot.isOccupied() && slot.getParkedVehicle() != null) {
                Vehicle v = slot.getParkedVehicle();
                if (v.getLicensePlate().equalsIgnoreCase(vehicle.getLicensePlate())) {
                    throw new DuplicateEntryException("License Plate '" + vehicle.getLicensePlate() + "' is already parked!");
                }
                if (v.getPhoneNumber().equalsIgnoreCase(vehicle.getPhoneNumber())) {
                    throw new DuplicateEntryException("Phone Number '" + vehicle.getPhoneNumber() + "' is already registered!");
                }
            }
        }

        for (ParkingSlot slot : slots) {
            if (!slot.isOccupied() && slot.getSupportedType().equalsIgnoreCase(vehicle.getVehicleType())) {
                slot.parkVehicle(vehicle);
                saveRecordToFile("PARK", vehicle, slot.getSlotId(), 0, 0, "ACTIVE");
                saveActiveStateToFile();
                return slot.getSlotId();
            }
        }
        throw new SlotUnavailableException("No empty slot available for " + vehicle.getVehicleType());
    }

    public String unparkVehicleAndGetReceipt(String licensePlate, String paymentMethod) throws VehicleNotFoundException {
        for (ParkingSlot slot : slots) {
            if (slot.isOccupied() && slot.getParkedVehicle().getLicensePlate().equalsIgnoreCase(licensePlate.trim())) {
                Vehicle v = slot.getParkedVehicle();
                long exitTime = System.currentTimeMillis();

                long durationMillis = exitTime - v.getEntryTime();
                long hours = Math.max(1, (long) Math.ceil(durationMillis / (1000.0 * 3600.0)));

                double totalFee = v.calculateFee(hours);
                slot.vacateSlot();

                saveRecordToFile("UNPARK", v, slot.getSlotId(), totalFee, hours, paymentMethod);
                saveActiveStateToFile();

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
                StringBuilder sb = new StringBuilder();
                sb.append("==============================================\n");
                sb.append("         VISTA MALL PARKING RECEIPT           \n");
                sb.append("==============================================\n");
                sb.append(" Owner Name    : ").append(v.getOwnerName()).append("\n");
                sb.append(" Contact No.   : ").append(v.getPhoneNumber()).append("\n");
                sb.append(" Vehicle Type  : ").append(v.getVehicleType()).append("\n");
                sb.append(" Model / Color : ").append(v.getModel()).append(" (").append(v.getColor()).append(")\n");
                sb.append(" License Plate : ").append(v.getLicensePlate()).append("\n");
                sb.append(" Assigned Slot : #").append(slot.getSlotId()).append("\n");
                sb.append("----------------------------------------------\n");
                sb.append(" Entry Time    : ").append(v.getFormattedEntryTime()).append("\n");
                sb.append(" Exit Time     : ").append(sdf.format(new Date(exitTime))).append("\n");
                sb.append(" Total Duration: ").append(hours).append(" Hour(s)\n");
                sb.append(" Hourly Rate   : Rs. ").append(v.getHourlyRate()).append("\n");
                sb.append(" Payment Mode  : ").append(paymentMethod).append("\n");
                sb.append("----------------------------------------------\n");
                sb.append(" TOTAL PAID    : Rs. ").append(totalFee).append("\n");
                sb.append("==============================================");
                return sb.toString();
            }
        }
        throw new VehicleNotFoundException("Vehicle with license plate '" + licensePlate + "' not found!");
    }

    private void saveRecordToFile(String action, Vehicle v, int slotId, double fee, long hours, String paymentMode) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            String record = String.format("%s | Plate: %s | Owner: %s | Phone: %s | Type: %s | Model: %s | Color: %s | Slot: %d | Hours: %d | Fee: Rs.%.2f | PayMode: %s",
                    action, v.getLicensePlate(), v.getOwnerName(), v.getPhoneNumber(), v.getVehicleType(), v.getModel(), v.getColor(), slotId, hours, fee, paymentMode);
            writer.write(record);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("File Writing Error: " + e.getMessage());
        }
    }

    private synchronized void saveActiveStateToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ACTIVE_FILE, false))) {
            for (ParkingSlot slot : slots) {
                if (slot.isOccupied() && slot.getParkedVehicle() != null) {
                    Vehicle v = slot.getParkedVehicle();
                    String line = String.format("%d;%s;%s;%s;%s;%s;%s;%d",
                            slot.getSlotId(), v.getVehicleType(), v.getLicensePlate(), v.getOwnerName(), v.getPhoneNumber(), v.getModel(), v.getColor(), v.getEntryTime());
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving active parking: " + e.getMessage());
        }
    }

    private void loadActiveParkingFromFile() {
        File file = new File(ACTIVE_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(";");
                if (parts.length == 8) {
                    int slotId = Integer.parseInt(parts[0]);
                    String type = parts[1];
                    String plate = parts[2];
                    String owner = parts[3];
                    String phone = parts[4];
                    String model = parts[5];
                    String color = parts[6];
                    long entryTime = Long.parseLong(parts[7]);

                    Vehicle v = null;
                    if ("Car".equalsIgnoreCase(type)) v = new Car(plate, owner, phone, model, color, entryTime);
                    else if ("Bike".equalsIgnoreCase(type)) v = new Bike(plate, owner, phone, model, color, entryTime);
                    else if ("Rickshaw".equalsIgnoreCase(type)) v = new Rickshaw(plate, owner, phone, color, entryTime);

                    for (ParkingSlot slot : slots) {
                        if (slot.getSlotId() == slotId) {
                            slot.parkVehicle(v);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading active state: " + e.getMessage());
        }
    }

    public void clearLogFile() {
        try (PrintWriter writer = new PrintWriter(LOG_FILE)) { writer.print(""); } catch (FileNotFoundException ignored) {}
        try (PrintWriter writer = new PrintWriter(ACTIVE_FILE)) { writer.print(""); } catch (FileNotFoundException ignored) {}

        for (ParkingSlot slot : slots) {
            slot.vacateSlot();
        }
    }
}

// ============================================================================
// 6. LOGIN FRAME (CLEAN SOLID LIGHT BLUE BACKGROUND)
// ============================================================================
class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private boolean isPasswordVisible = false;

    private final Color APP_BG = new Color(225, 238, 252);
    private final Color CARD_BORDER = new Color(218, 225, 233);
    private final Color TEXT_PRIMARY = new Color(18, 24, 38);
    private final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private final Color ACCENT_PRIMARY = new Color(13, 110, 253);

    public LoginFrame() {
        setTitle("Vista Mall Parking ");
        setSize(800, 650);
        setMinimumSize(new Dimension(480, 520));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        initCustomWindowIcon();

        // Main Panel with Clean Solid Light Blue Background
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(APP_BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                new EmptyBorder(30, 35, 30, 35)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- 1. BRANDING LOGO ---
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(13, 110, 253, 25));
                g2.fillRoundRect(0, 0, 68, 68, 18, 18);

                GradientPaint gp = new GradientPaint(2, 2, ACCENT_PRIMARY, 64, 64, new Color(10, 88, 202));
                g2.setPaint(gp);
                g2.fillRoundRect(2, 2, 64, 64, 16, 16);

                g2.setColor(new Color(255, 255, 255, 140));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(4, 4, 60, 60, 14, 14);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
                g2.drawString("V", 21, 47);
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(70, 70));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        card.add(logoPanel, gbc);

        // --- 2. TITLE & SUBTITLE ---
        JLabel lblTitle = new JLabel("VISTA MALL", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSubTitle = new JLabel("PARKING MANAGEMENT SYSTEM", JLabel.CENTER);
        lblSubTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSubTitle.setForeground(ACCENT_PRIMARY);

        gbc.gridy = 1; gbc.insets = new Insets(8, 0, 2, 0);
        card.add(lblTitle, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 20, 0);
        card.add(lblSubTitle, gbc);

        // --- 3. INPUT FORM FIELDS ---
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(TEXT_SECONDARY);

        txtUsername = createStyledTextField();

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(TEXT_SECONDARY);

        JPanel passContainer = createPasswordPanelWithEye();

        gbc.gridx = 0; gbc.gridy = 3; gbc.insets = new Insets(4, 0, 2, 0);
        card.add(lblUser, gbc);
        gbc.gridx = 0; gbc.gridy = 4; gbc.insets = new Insets(0, 0, 10, 0);
        card.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.insets = new Insets(4, 0, 2, 0);
        card.add(lblPass, gbc);
        gbc.gridx = 0; gbc.gridy = 6; gbc.insets = new Insets(0, 0, 18, 0);
        card.add(passContainer, gbc);

        // --- 4. ACTION BUTTON ---
        JButton btnLogin = new JButton("Login") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(10, 88, 202));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(10, 88, 202));
                } else {
                    g2.setColor(ACCENT_PRIMARY);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(280, 42));

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 0, 5, 0);
        card.add(btnLogin, gbc);

        btnLogin.addActionListener(e -> {
            String user = txtUsername.getText().trim();
            String pass = new String(txtPassword.getPassword()).trim();

            if (user.equals("admin") && pass.equals("1234")) {
                dispose();
                new Main().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password!\n", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        mainPanel.add(card);
        add(mainPanel);
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField(18) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        tf.setOpaque(false);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setPreferredSize(new Dimension(280, 42));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_PRIMARY, 2),
                        BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CARD_BORDER, 1),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        return tf;
    }

    private JPanel createPasswordPanelWithEye() {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(280, 42));
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(2, 4, 2, 10)
        ));

        txtPassword = new JPasswordField(18);
        txtPassword.setOpaque(false);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setForeground(TEXT_PRIMARY);
        txtPassword.setCaretColor(TEXT_PRIMARY);
        txtPassword.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JButton btnEye = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(isPasswordVisible ? ACCENT_PRIMARY : new Color(110, 115, 125));
                g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                g2.drawArc(1, h / 2 - 3, w - 2, 6, 0, 180);
                g2.drawArc(1, h / 2 - 3, w - 2, 6, 180, 180);

                if (isPasswordVisible) {
                    g2.fillOval(w / 2 - 2, h / 2 - 2, 4, 4);
                } else {
                    g2.drawOval(w / 2 - 1, h / 2 - 1, 2, 2);
                    g2.drawLine(2, h - 2, w - 2, 2);
                }
            }
        };
        btnEye.setPreferredSize(new Dimension(16, 16));
        btnEye.setFocusPainted(false);
        btnEye.setContentAreaFilled(false);
        btnEye.setBorderPainted(false);
        btnEye.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEye.setToolTipText("Show/Hide Password");

        btnEye.addActionListener(e -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('●');
            }
            btnEye.repaint();
        });

        txtPassword.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                wrapper.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_PRIMARY, 2),
                        BorderFactory.createEmptyBorder(1, 3, 1, 9)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                wrapper.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CARD_BORDER, 1),
                        BorderFactory.createEmptyBorder(2, 4, 2, 10)
                ));
            }
        });

        wrapper.add(txtPassword, BorderLayout.CENTER);
        wrapper.add(btnEye, BorderLayout.EAST);

        return wrapper;
    }

    private void initCustomWindowIcon() {
        try {
            BufferedImage appIcon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = appIcon.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(ACCENT_PRIMARY);
            g2.fillRoundRect(0, 0, 32, 32, 8, 8);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 19));
            g2.drawString("V", 10, 23);

            g2.dispose();
            setIconImage(appIcon);
        } catch (Exception ignored) {}
    }
}

// ============================================================================
// 7. MAIN APPLICATION GUI FRAME
// ============================================================================
public class Main extends JFrame {
    private ParkingLotManager manager;
    private DefaultTableModel tableModel;
    private JTable slotTable;
    private JLabel lblTotalCount, lblOccupiedCount, lblAvailableCount;
    private JTextArea txtGreenLogs, txtRedLogs;

    private final Color APP_BG = new Color(245, 247, 250);
    private final Color CARD_BG = new Color(255, 255, 255);
    private final Color CARD_BORDER = new Color(218, 225, 233);
    private final Color HEADER_BG = new Color(235, 240, 245);

    private final Color TEXT_PRIMARY = new Color(18, 24, 38);
    private final Color TEXT_SECONDARY = new Color(108, 117, 125);

    private final Color PASTEL_GREEN = new Color(92, 184, 129);
    private final Color GREEN_PRESS = new Color(74, 148, 103);

    private final Color REFINED_RED = new Color(229, 95, 95);
    private final Color RED_PRESS = new Color(195, 75, 75);

    private final Color ACCENT_PRIMARY = new Color(13, 110, 253);

    public Main() {
        manager = new ParkingLotManager();

        setTitle("Vista Mall Parking Management System");
        setSize(1280, 840);
        setMinimumSize(new Dimension(850, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        initCustomWindowIcon();

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(APP_BG);

        mainContainer.add(createTopHeaderPanel(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(APP_BG);
        tabbedPane.setForeground(TEXT_PRIMARY);
        tabbedPane.setFocusable(false);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {}
            @Override protected void paintFocusIndicator(Graphics g, int tabIndex, Rectangle[] rects, int tabIndex2, Rectangle iconRect, Rectangle textRect, boolean isSelected) {}
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(APP_BG);
                g2.fillRect(x, y, w, h);

                if (isSelected) {
                    g2.setColor(ACCENT_PRIMARY);
                    g2.fillRect(x + 6, y + h - 3, w - 12, 3);
                }
                g2.dispose();
            }
            @Override protected Insets getTabInsets(int tabPlacement, int tabIndex) { return new Insets(10, 20, 10, 20); }
        });

        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Park Vehicle", createParkPanel());
        tabbedPane.addTab("Checkout & Bill", createCheckoutPanel());
        tabbedPane.addTab("System Audit Logs", createLogsPanel());

        mainContainer.add(tabbedPane, BorderLayout.CENTER);
        add(mainContainer);

        refreshTableData();
    }

    private void initCustomWindowIcon() {
        try {
            BufferedImage appIcon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = appIcon.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(ACCENT_PRIMARY);
            g2.fillRoundRect(0, 0, 32, 32, 8, 8);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 19));
            g2.drawString("V", 10, 23);

            g2.dispose();
            setIconImage(appIcon);
        } catch (Exception ignored) {}
    }

    private JPanel createTopHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(218, 225, 233));
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        headerPanel.setPreferredSize(new Dimension(getWidth(), 85));
        headerPanel.setBorder(new EmptyBorder(15, 35, 15, 35));

        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(13, 110, 253, 25));
                g2.fillRoundRect(0, 0, 52, 52, 14, 14);

                GradientPaint gp = new GradientPaint(2, 2, ACCENT_PRIMARY, 50, 50, new Color(10, 88, 202));
                g2.setPaint(gp);
                g2.fillRoundRect(2, 2, 48, 48, 12, 12);

                g2.setColor(new Color(255, 255, 255, 140));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(3, 3, 46, 46, 10, 10);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                g2.drawString("V", 15, 35);
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(56, 54));

        JLabel titleLabel = new JLabel("VISTA MALL PARKING");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subTitleLabel = new JLabel("Smart & Secure Parking Management System");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitleLabel.setForeground(TEXT_SECONDARY);

        JPanel titleContainer = new JPanel(new GridLayout(2, 1, 0, -1));
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel);
        titleContainer.add(subTitleLabel);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftHeader.setOpaque(false);
        leftHeader.add(logoPanel);
        leftHeader.add(titleContainer);

        // --- TOP RIGHT LOGOUT BUTTON ---
        JButton btnLogout = createExecutiveButton("Logout", REFINED_RED, RED_PRESS);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setPreferredSize(new Dimension(110, 38));

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out of the system?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        rightHeader.setOpaque(false);
        rightHeader.add(btnLogout);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(APP_BG);

        JPanel cardsContainer = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsContainer.setOpaque(false);

        lblTotalCount = new JLabel("130", JLabel.LEFT);
        lblOccupiedCount = new JLabel("0", JLabel.LEFT);
        lblAvailableCount = new JLabel("130", JLabel.LEFT);

        cardsContainer.add(createMetricCardWithIcon("TOTAL CAPACITY", lblTotalCount, new Color(224, 238, 255), new Color(13, 110, 253), "BUILDING"));
        cardsContainer.add(createMetricCardWithIcon("OCCUPIED SLOTS", lblOccupiedCount, new Color(255, 235, 238), new Color(220, 53, 69), "CAR"));
        cardsContainer.add(createMetricCardWithIcon("AVAILABLE SLOTS", lblAvailableCount, new Color(209, 238, 228), new Color(25, 135, 84), "CHECK"));

        panel.add(cardsContainer, BorderLayout.NORTH);

        String[] columns = {"Slot ID", "Slot Category", "Status", "License Plate", "Model / Color", "Owner Name", "Contact Number"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        slotTable = new JTable(tableModel);
        slotTable.setRowHeight(46);
        slotTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        slotTable.setBackground(CARD_BG);
        slotTable.setForeground(TEXT_PRIMARY);
        slotTable.setGridColor(new Color(235, 240, 245));
        slotTable.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = slotTable.getTableHeader();
        header.setPreferredSize(new Dimension(0, 48));
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(HEADER_BG);
        header.setForeground(TEXT_PRIMARY);
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(HEADER_BG);
                c.setForeground(TEXT_PRIMARY);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(218, 225, 233)));
                return c;
            }
        });

        // Dynamic Responsive Renderer
        slotTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setHorizontalAlignment(JLabel.CENTER);

                if (column == 2) {
                    String status = (String) value;
                    JLabel pill = new JLabel(status, JLabel.CENTER) {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g;
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            if ("OCCUPIED".equals(status)) {
                                g2.setColor(new Color(255, 235, 238));
                            } else {
                                g2.setColor(new Color(209, 238, 228));
                            }

                            int paddingX = Math.max(6, (getWidth() - 95) / 2);
                            g2.fillRoundRect(paddingX, 9, getWidth() - (paddingX * 2), getHeight() - 18, 12, 12);
                            super.paintComponent(g);
                        }
                    };
                    pill.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    if ("OCCUPIED".equals(status)) {
                        pill.setForeground(new Color(176, 42, 55));
                    } else {
                        pill.setForeground(new Color(20, 108, 67));
                    }
                    if (isSelected) {
                        pill.setBackground(new Color(224, 238, 255));
                    } else {
                        pill.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    }
                    return pill;
                }

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(224, 238, 255));
                    c.setForeground(TEXT_PRIMARY);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(TEXT_PRIMARY);
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(slotTable);
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMetricCardWithIcon(String title, JLabel valueLabel, Color iconBgColor, Color iconColor, String iconType) {
        JPanel card = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            }
        };
        card.setPreferredSize(new Dimension(0, 115));
        card.setOpaque(false);

        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2.setColor(iconColor);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                if (iconType.equals("BUILDING")) {
                    g2.drawRect(12, 11, 18, 22);
                    g2.fillRect(16, 16, 3, 4);
                    g2.fillRect(23, 16, 3, 4);
                    g2.fillRect(16, 24, 3, 4);
                    g2.fillRect(23, 24, 3, 4);
                } else if (iconType.equals("CAR")) {
                    g2.drawRoundRect(6, 17, 28, 10, 5, 5);
                    g2.drawLine(11, 17, 14, 11);
                    g2.drawLine(14, 11, 26, 11);
                    g2.drawLine(26, 11, 29, 17);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(10, 24, 7, 7);
                    g2.fillOval(23, 24, 7, 7);
                    g2.setColor(iconColor);
                    g2.drawOval(10, 24, 7, 7);
                    g2.drawOval(23, 24, 7, 7);
                } else if (iconType.equals("CHECK")) {
                    g2.drawOval(9, 9, 24, 24);
                    g2.drawLine(15, 21, 19, 25);
                    g2.drawLine(19, 25, 27, 15);
                }
            }
        };
        iconPanel.setBounds(20, 22, 42, 42);
        iconPanel.setOpaque(false);
        card.add(iconPanel);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(TEXT_SECONDARY);
        titleLbl.setBounds(78, 20, 200, 15);
        card.add(titleLbl);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setBounds(78, 38, 200, 35);
        card.add(valueLabel);

        return card;
    }

    private JPanel createParkPanel() {
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(APP_BG);

        JPanel card = createStyledCardPanel();
        card.setBorder(new EmptyBorder(30, 45, 30, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblHeader = new JLabel("VEHICLE CHECK-IN FORM", JLabel.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(lblHeader, gbc);

        gbc.gridwidth = 1;
        String[] types = {"Car", "Bike", "Rickshaw"};
        JComboBox<String> cmbType = new JComboBox<>(types);
        styleComboBox(cmbType);
        cmbType.setPreferredSize(new Dimension(320, 38));

        JTextField txtFirstName = createExecutiveTextField(30, PASTEL_GREEN);
        JTextField txtLastName = createExecutiveTextField(30, PASTEL_GREEN);

        JTextField txtPhone = createExecutiveTextField(30, PASTEL_GREEN);
        JTextField txtModel = createExecutiveTextField(30, PASTEL_GREEN);
        JTextField txtColor = createExecutiveTextField(30, PASTEL_GREEN);
        JTextField txtPlate = createExecutiveTextField(30, PASTEL_GREEN);

        addFormField(card, gbc, 1, "Vehicle Category:", cmbType);
        addFormField(card, gbc, 2, "First Name:", txtFirstName);
        addFormField(card, gbc, 3, "Last Name:", txtLastName);
        addFormField(card, gbc, 4, "Phone:", txtPhone);
        addFormField(card, gbc, 5, "Vehicle Model:", txtModel);
        addFormField(card, gbc, 6, "Vehicle Color:", txtColor);
        addFormField(card, gbc, 7, "License Plate:", txtPlate);

        JButton btnSubmit = createExecutiveButton("Park Vehicle & Issue Ticket", PASTEL_GREEN, GREEN_PRESS);
        btnSubmit.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        card.add(btnSubmit, gbc);

        cmbType.addActionListener(e -> {
            String selected = (String) cmbType.getSelectedItem();
            boolean hasModel = "Car".equalsIgnoreCase(selected) || "Bike".equalsIgnoreCase(selected);
            txtModel.setEnabled(hasModel);
            if (!hasModel) txtModel.setText("-");
        });

        btnSubmit.addActionListener(e -> {
            resetTextFieldBorders(txtFirstName, txtLastName, txtPhone, txtModel, txtColor, txtPlate);

            String type = (String) cmbType.getSelectedItem();
            String firstName = txtFirstName.getText().trim();
            String lastName = txtLastName.getText().trim();
            String phone = txtPhone.getText().trim();
            String model = txtModel.getText().trim();
            String color = txtColor.getText().trim();
            String plate = txtPlate.getText().trim();

            if (!firstName.matches("^[a-zA-Z]+$")) {
                txtFirstName.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.RED, 2), BorderFactory.createEmptyBorder(6, 11, 6, 11)));
                try { throw new InvalidNameException("First Name must contain alphabets only!"); }
                catch (InvalidNameException ex) { showExecutiveMessageDialog(ex.getMessage(), "First Name Exception", JOptionPane.ERROR_MESSAGE); return; }
            }

            if (!lastName.matches("^[a-zA-Z]+$")) {
                txtLastName.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.RED, 2), BorderFactory.createEmptyBorder(6, 11, 6, 11)));
                try { throw new InvalidNameException("Last Name must contain alphabets only!"); }
                catch (InvalidNameException ex) { showExecutiveMessageDialog(ex.getMessage(), "Last Name Exception", JOptionPane.ERROR_MESSAGE); return; }
            }

            String formattedFirstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();
            String formattedLastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();
            String fullOwnerName = formattedFirstName + " " + formattedLastName;

            boolean isValidPakPhone = phone.matches("^03\\d{9}$") || phone.matches("^\\+92\\d{10}$");
            if (!isValidPakPhone) {
                txtPhone.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.RED, 2), BorderFactory.createEmptyBorder(6, 11, 6, 11)));
                showExecutiveMessageDialog("Invalid Phone Number!\nMust start with '03' (11 digits) OR '+92' (13 chars).", "Phone Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!plate.matches("^[A-Z]{3}\\d{4}$")) {
                txtPlate.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.RED, 2), BorderFactory.createEmptyBorder(6, 11, 6, 11)));
                showExecutiveMessageDialog("Invalid License Plate Format!\nMust start with EXACTLY 3 CAPITAL alphabets followed by 4 numbers (e.g. ABC1234).", "Plate Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (color.isEmpty()) {
                showExecutiveMessageDialog("Please fill in all required fields!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<String> validColors = Arrays.asList("black", "white", "red", "blue", "grey", "gray", "silver", "green", "yellow", "brown", "maroon", "orange", "purple", "pink", "gold");
            List<String> carModels = Arrays.asList("civic", "city", "corolla", "alto", "swift", "brv", "accord", "fortuner", "mehran", "cultus", "vitz", "bmw", "audi", "mercedes", "elantra", "sportage", "prius", "yaris");
            List<String> bikeModels = Arrays.asList("cd70", "70", "cg125", "125", "ybr", "gs150", "cb150", "pridor", "hayabusa", "ninja", "unique", "superpower");
            List<String> genericBrands = Arrays.asList("honda", "suzuki", "yamaha", "toyota", "hyundai", "kia");

            String checkColor = color.toLowerCase().trim();
            String checkModel = model.toLowerCase().replaceAll("[\\s\\-_]", "");

            if (!validColors.contains(checkColor)) {
                txtColor.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.RED, 2), BorderFactory.createEmptyBorder(6, 11, 6, 11)));
                try { throw new InvalidColorException("'" + color + "' is NOT a valid color name!"); }
                catch (InvalidColorException ex) { showExecutiveMessageDialog(ex.getMessage(), "Color Validation Exception", JOptionPane.ERROR_MESSAGE); return; }
            }

            if (carModels.contains(checkColor) || bikeModels.contains(checkColor)) {
                txtColor.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.RED, 2), BorderFactory.createEmptyBorder(6, 11, 6, 11)));
                try { throw new InvalidColorException("'" + color + "' is a Vehicle Model, NOT a Color!"); }
                catch (InvalidColorException ex) { showExecutiveMessageDialog(ex.getMessage(), "Color Validation Exception", JOptionPane.ERROR_MESSAGE); return; }
            }

            if (!"Rickshaw".equalsIgnoreCase(type)) {
                if (checkModel.matches("^\\d+$")) {
                    txtModel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.RED, 2), BorderFactory.createEmptyBorder(6, 11, 6, 11)));
                    try { throw new InvalidModelException("Vehicle Model cannot be purely numbers!"); }
                    catch (InvalidModelException ex) { showExecutiveMessageDialog(ex.getMessage(), "Model Validation Exception", JOptionPane.ERROR_MESSAGE); return; }
                }

                if (validColors.contains(checkModel)) {
                    txtModel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.RED, 2), BorderFactory.createEmptyBorder(6, 11, 6, 11)));
                    try { throw new InvalidModelException("'" + model + "' is a Color Name, NOT a Vehicle Model!"); }
                    catch (InvalidModelException ex) { showExecutiveMessageDialog(ex.getMessage(), "Model Validation Exception", JOptionPane.ERROR_MESSAGE); return; }
                }

                if (genericBrands.contains(checkModel)) {
                    txtModel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.RED, 2), BorderFactory.createEmptyBorder(6, 11, 6, 11)));
                    try { throw new InvalidModelException("'" + model + "' is a generic Brand! Please enter specific Model."); }
                    catch (InvalidModelException ex) { showExecutiveMessageDialog(ex.getMessage(), "Model Validation Exception", JOptionPane.ERROR_MESSAGE); return; }
                }

                if ("Car".equalsIgnoreCase(type)) {
                    for (String bm : bikeModels) {
                        if (checkModel.contains(bm)) {
                            txtModel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.RED, 2), BorderFactory.createEmptyBorder(6, 11, 6, 11)));
                            try { throw new InvalidModelException("Cannot enter Bike Model ('" + model + "') for a Car!"); }
                            catch (InvalidModelException ex) { showExecutiveMessageDialog(ex.getMessage(), "Model Validation Exception", JOptionPane.ERROR_MESSAGE); return; }
                        }
                    }
                    boolean validCar = false;
                    for (String cm : carModels) { if (checkModel.contains(cm)) { validCar = true; break; } }
                    if (!validCar) {
                        txtModel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.RED, 2), BorderFactory.createEmptyBorder(6, 11, 6, 11)));
                        try { throw new InvalidModelException("'" + model + "' is NOT a recognized Car Model!"); }
                        catch (InvalidModelException ex) { showExecutiveMessageDialog(ex.getMessage(), "Model Validation Exception", JOptionPane.ERROR_MESSAGE); return; }
                    }
                } else if ("Bike".equalsIgnoreCase(type)) {
                    for (String cm : carModels) {
                        if (checkModel.contains(cm)) {
                            txtModel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.RED, 2), BorderFactory.createEmptyBorder(6, 11, 6, 11)));
                            try { throw new InvalidModelException("Cannot enter Car Model ('" + model + "') for a Bike!"); }
                            catch (InvalidModelException ex) { showExecutiveMessageDialog(ex.getMessage(), "Model Validation Exception", JOptionPane.ERROR_MESSAGE); return; }
                        }
                    }
                    boolean validBike = false;
                    for (String bm : bikeModels) { if (checkModel.contains(bm)) { validBike = true; break; } }
                    if (!validBike) {
                        txtModel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.RED, 2), BorderFactory.createEmptyBorder(6, 11, 6, 11)));
                        try { throw new InvalidModelException("'" + model + "' is NOT a recognized Bike Model!"); }
                        catch (InvalidModelException ex) { showExecutiveMessageDialog(ex.getMessage(), "Model Validation Exception", JOptionPane.ERROR_MESSAGE); return; }
                    }
                }
            }

            Vehicle v = null;
            if ("Car".equalsIgnoreCase(type)) v = new Car(plate, fullOwnerName, phone, model, color);
            else if ("Bike".equalsIgnoreCase(type)) v = new Bike(plate, fullOwnerName, phone, model, color);
            else if ("Rickshaw".equalsIgnoreCase(type)) v = new Rickshaw(plate, fullOwnerName, phone, color);

            try {
                int assignedSlot = manager.parkVehicle(v);
                showExecutiveMessageDialog("Vehicle Parked Successfully at Slot #" + assignedSlot + "\nEntry Time: " + v.getFormattedEntryTime(), "Entry Confirmed", JOptionPane.INFORMATION_MESSAGE);

                txtFirstName.setText("");
                txtLastName.setText("");
                txtPhone.setText("");
                txtModel.setText("");
                txtColor.setText("");
                txtPlate.setText("");
                refreshTableData();
            } catch (SlotUnavailableException ex) {
                showExecutiveMessageDialog(ex.getMessage(), "Slot Full Warning", JOptionPane.WARNING_MESSAGE);
            } catch (DuplicateEntryException ex) {
                showExecutiveMessageDialog(ex.getMessage(), "Duplicate Entry Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        outerPanel.add(card);
        return outerPanel;
    }

    private void resetTextFieldBorders(JTextField... fields) {
        for (JTextField tf : fields) {
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(218, 225, 233), 1),
                    BorderFactory.createEmptyBorder(7, 12, 7, 12)
            ));
        }
    }

    private JPanel createCheckoutPanel() {
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(APP_BG);

        JPanel card = createStyledCardPanel();
        card.setBorder(new EmptyBorder(30, 45, 30, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblHeader = new JLabel("VEHICLE CHECKOUT & BILLING", JLabel.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setForeground(REFINED_RED);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(lblHeader, gbc);

        gbc.gridwidth = 1;
        JTextField txtCheckoutPlate = createExecutiveTextField(30, REFINED_RED);

        JRadioButton chkCash = new JRadioButton("Cash Payment", true);
        JRadioButton chkOnline = new JRadioButton("Online / Card Payment");
        chkCash.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkOnline.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkCash.setOpaque(false);
        chkOnline.setOpaque(false);

        ButtonGroup payGroup = new ButtonGroup();
        payGroup.add(chkCash);
        payGroup.add(chkOnline);

        JPanel payPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        payPanel.setOpaque(false);
        payPanel.add(chkCash);
        payPanel.add(chkOnline);

        JPanel qrContainer = new JPanel(new BorderLayout(5, 5));
        qrContainer.setOpaque(false);
        qrContainer.setVisible(false);

        JPanel qrBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                g2.setColor(TEXT_PRIMARY);
                int size = 110;
                int startX = (getWidth() - size) / 2;
                int startY = 15;

                g2.fillRect(startX, startY, 32, 32);
                g2.fillRect(startX + size - 32, startY, 32, 32);
                g2.fillRect(startX, startY + size - 32, 32, 32);

                g2.setColor(Color.WHITE);
                g2.fillRect(startX + 5, startY + 5, 22, 22);
                g2.fillRect(startX + size - 27, startY + 5, 22, 22);
                g2.fillRect(startX + 5, startY + size - 27, 22, 22);

                g2.setColor(TEXT_PRIMARY);
                g2.fillRect(startX + 10, startY + 10, 12, 12);
                g2.fillRect(startX + size - 22, startY + 10, 12, 12);
                g2.fillRect(startX + 10, startY + size - 22, 12, 12);

                g2.fillRect(startX + 40, startY + 5, 12, 12);
                g2.fillRect(startX + 58, startY + 10, 15, 8);
                g2.fillRect(startX + 42, startY + 25, 20, 10);
                g2.fillRect(startX + 10, startY + 42, 15, 15);
                g2.fillRect(startX + 35, startY + 45, 10, 20);
                g2.fillRect(startX + 52, startY + 40, 25, 25);
                g2.fillRect(startX + 85, startY + 45, 18, 12);
                g2.fillRect(startX + 45, startY + 75, 18, 18);
                g2.fillRect(startX + 70, startY + 70, 15, 25);

                g2.setColor(ACCENT_PRIMARY);
                g2.fillOval(startX + 43, startY + 43, 24, 24);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString("Rs", startX + 48, startY + 60);
            }
        };
        qrBox.setPreferredSize(new Dimension(220, 145));
        qrBox.setOpaque(false);

        JLabel lblQrInfo = new JLabel("Scan QR Code to Pay via Easypaisa / JazzCash / Bank App", JLabel.CENTER);
        lblQrInfo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblQrInfo.setForeground(ACCENT_PRIMARY);

        qrContainer.add(qrBox, BorderLayout.CENTER);
        qrContainer.add(lblQrInfo, BorderLayout.SOUTH);

        chkOnline.addActionListener(e -> {
            qrContainer.setVisible(true);
            card.revalidate();
            card.repaint();
        });

        chkCash.addActionListener(e -> {
            qrContainer.setVisible(false);
            card.revalidate();
            card.repaint();
        });

        addFormField(card, gbc, 1, "License Plate:", txtCheckoutPlate);
        addFormField(card, gbc, 2, "Payment Method:", payPanel);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        card.add(qrContainer, gbc);

        JButton btnCheckout = createExecutiveButton("Process Checkout & Generate Receipt", REFINED_RED, RED_PRESS);
        btnCheckout.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        card.add(btnCheckout, gbc);

        btnCheckout.addActionListener(e -> {
            String plate = txtCheckoutPlate.getText().trim();
            if (plate.isEmpty()) {
                showExecutiveMessageDialog("Please enter a valid License Plate!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String payMethod = chkCash.isSelected() ? "Cash" : "Online / Card (QR Scanned)";

            try {
                String receipt = manager.unparkVehicleAndGetReceipt(plate, payMethod);

                JTextArea txtReceipt = new JTextArea(receipt);
                txtReceipt.setFont(new Font("Monospaced", Font.PLAIN, 12));
                txtReceipt.setEditable(false);
                txtReceipt.setBackground(new Color(248, 250, 252));
                txtReceipt.setForeground(TEXT_PRIMARY);

                JPanel receiptDialogPanel = new JPanel(new BorderLayout(10, 10));
                receiptDialogPanel.add(new JScrollPane(txtReceipt), BorderLayout.CENTER);

                JButton btnPrint = createExecutiveButton("Print Ticket", PASTEL_GREEN, GREEN_PRESS);
                btnPrint.setForeground(Color.WHITE);

                JButton btnDownload = createExecutiveButton("Download Receipt (.txt)", ACCENT_PRIMARY, new Color(10, 88, 202));
                btnDownload.setForeground(Color.WHITE);

                btnPrint.addActionListener(pEvt -> {
                    showExecutiveMessageDialog("Receipt sent to printer successfully!", "Print Status", JOptionPane.INFORMATION_MESSAGE);
                });

                btnDownload.addActionListener(dlEvt -> {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setSelectedFile(new File("Receipt_" + plate + ".txt"));
                    int userSelection = fileChooser.showSaveDialog(this);

                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        File fileToSave = fileChooser.getSelectedFile();
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                            writer.write(receipt);
                            showExecutiveMessageDialog("Receipt downloaded successfully!\nSaved at: " + fileToSave.getAbsolutePath(), "Download Success", JOptionPane.INFORMATION_MESSAGE);
                        } catch (IOException ex) {
                            showExecutiveMessageDialog("Error saving receipt file: " + ex.getMessage(), "Download Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
                btnPanel.setOpaque(false);
                btnPanel.add(btnPrint);
                btnPanel.add(btnDownload);
                receiptDialogPanel.add(btnPanel, BorderLayout.SOUTH);

                JOptionPane.showMessageDialog(this, receiptDialogPanel, "Official Exit Receipt", JOptionPane.INFORMATION_MESSAGE);
                txtCheckoutPlate.setText("");
                refreshTableData();
            } catch (VehicleNotFoundException ex) {
                showExecutiveMessageDialog(ex.getMessage(), "Checkout Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        outerPanel.add(card);
        return outerPanel;
    }

    private JPanel createLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(APP_BG);

        JPanel topHeaderBar = new JPanel(new GridBagLayout());
        topHeaderBar.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblHeader = new JLabel("MALL PARKING REAL-TIME AUDIT LOGS");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeader.setForeground(TEXT_PRIMARY);
        topHeaderBar.add(lblHeader, gbc);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        btnGroup.setOpaque(false);

        JButton btnRefreshLogs = createExecutiveButton("Refresh Audit Logs", ACCENT_PRIMARY, new Color(10, 88, 202));
        btnRefreshLogs.setForeground(Color.WHITE);

        JButton btnDeleteLogs = createExecutiveButton("Delete All Audit Records", REFINED_RED, RED_PRESS);
        btnDeleteLogs.setForeground(Color.WHITE);

        btnGroup.add(btnRefreshLogs);
        btnGroup.add(btnDeleteLogs);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        topHeaderBar.add(btnGroup, gbc);

        panel.add(topHeaderBar, BorderLayout.NORTH);

        txtGreenLogs = new JTextArea();
        txtGreenLogs.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtGreenLogs.setEditable(false);
        txtGreenLogs.setForeground(new Color(20, 108, 67));

        JScrollPane greenScroll = new JScrollPane(txtGreenLogs);
        greenScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(25, 135, 84), 2), " ACTIVE PARKED VEHICLES "));

        txtRedLogs = new JTextArea();
        txtRedLogs.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtRedLogs.setEditable(false);
        txtRedLogs.setForeground(new Color(176, 42, 55));

        JScrollPane redScroll = new JScrollPane(txtRedLogs);
        redScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 53, 69), 2), " UNPARKED "));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, greenScroll, redScroll);
        splitPane.setResizeWeight(0.5);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        panel.add(splitPane, BorderLayout.CENTER);

        btnRefreshLogs.addActionListener(e -> {
            loadLogsFromFile();
            showExecutiveMessageDialog("Audit Logs Refreshed Successfully!", "Refreshed", JOptionPane.INFORMATION_MESSAGE);
        });

        btnDeleteLogs.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete ALL file logs permanently?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                manager.clearLogFile();
                refreshTableData();
                showExecutiveMessageDialog("All Audit Logs and Active Parking Records Deleted Successfully!", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        loadLogsFromFile();
        return panel;
    }

    private void loadLogsFromFile() {
        File file = new File("parking_records.txt");
        if (!file.exists()) {
            if (txtGreenLogs != null) txtGreenLogs.setText("No records found.");
            if (txtRedLogs != null) txtRedLogs.setText("No records found.");
            return;
        }

        StringBuilder greenSb = new StringBuilder();
        StringBuilder redSb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("PARK")) {
                    greenSb.append(line).append("\n\n");
                } else if (line.startsWith("UNPARK")) {
                    redSb.append(line).append("\n\n");
                }
            }
            if (txtGreenLogs != null) txtGreenLogs.setText(greenSb.length() > 0 ? greenSb.toString() : "No Active Parked Records.");
            if (txtRedLogs != null) txtRedLogs.setText(redSb.length() > 0 ? redSb.toString() : "No Unparked Records.");
        } catch (IOException e) {
            if (txtGreenLogs != null) txtGreenLogs.setText("Error reading log file.");
            if (txtRedLogs != null) txtRedLogs.setText("Error reading log file.");
        }
    }

    private void refreshTableData() {
        tableModel.setRowCount(0);
        int occupied = 0;
        List<ParkingSlot> slots = manager.getSlots();

        for (ParkingSlot slot : slots) {
            String status = slot.isOccupied() ? "OCCUPIED" : "AVAILABLE";
            if (slot.isOccupied()) occupied++;

            String plate = "-";
            String modelColor = "-";
            String owner = "-";
            String phone = "-";

            if (slot.isOccupied() && slot.getParkedVehicle() != null) {
                Vehicle v = slot.getParkedVehicle();
                plate = v.getLicensePlate();
                owner = v.getOwnerName();
                phone = v.getPhoneNumber();
                if (!v.getModel().equals("-")) {
                    modelColor = v.getModel() + " / " + v.getColor();
                } else {
                    modelColor = v.getColor();
                }
            }

            tableModel.addRow(new Object[]{
                    "#" + slot.getSlotId(),
                    slot.getSupportedType(),
                    status,
                    plate,
                    modelColor,
                    owner,
                    phone
            });
        }

        int total = slots.size();
        int available = total - occupied;

        if (lblTotalCount != null) lblTotalCount.setText(String.valueOf(total));
        if (lblOccupiedCount != null) lblOccupiedCount.setText(String.valueOf(occupied));
        if (lblAvailableCount != null) lblAvailableCount.setText(String.valueOf(available));

        loadLogsFromFile();
    }

    private JPanel createStyledCardPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    private JTextField createExecutiveTextField(int columns, Color focusColor) {
        JTextField tf = new JTextField(columns) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        tf.setOpaque(false);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setPreferredSize(new Dimension(320, 38));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 225, 233), 1),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));

        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(focusColor, 2),
                        BorderFactory.createEmptyBorder(6, 11, 6, 11)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(218, 225, 233), 1),
                        BorderFactory.createEmptyBorder(7, 12, 7, 12)
                ));
            }
        });
        return tf;
    }

    private void styleComboBox(JComboBox<String> cmb) {
        cmb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmb.setBackground(Color.WHITE);
        cmb.setForeground(TEXT_PRIMARY);
        cmb.setFocusable(false);
    }

    private JButton createExecutiveButton(String text, Color baseColor, Color pressColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(pressColor);
                } else if (getModel().isRollover()) {
                    g2.setColor(pressColor);
                } else {
                    g2.setColor(baseColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(11, 22, 11, 22));
        return btn;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_SECONDARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }

    private void showExecutiveMessageDialog(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}