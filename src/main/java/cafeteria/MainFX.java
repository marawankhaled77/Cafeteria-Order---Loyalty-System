package cafeteria;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class MainFX extends Application {
    // Core managers/repositories
    private FileStudentRepository studentRepo = new FileStudentRepository();
    private InMemoryMenuProvider menuProvider = new InMemoryMenuProvider();
    InMemoryOrderRepository orderRepo = new InMemoryOrderRepository("order.txt");
    private PointsCalculator calculator = new BasicPointsCalculator(10.0);
    private LoyaltyProgram loyalty = new LoyaltyProgram(calculator, studentRepo);
    private NotificationService notifications = new NotificationService();
    private OrderProcessor orders = new OrderProcessor(menuProvider, orderRepo, loyalty, notifications);
    private MenuManager menuMgr = new MenuManager(menuProvider);
    private StudentManager studentMgr = new StudentManager(studentRepo);
    private ReportService reports = new ReportService(orderRepo);

    private Stage mainStage;
    private Scene welcomeScene, studentLoginScene, studentMainScene, adminLoginScene, adminMainScene;

    private Student loggedInStudent = null;

    // For student cart logic and menu grid
    private List<OrderLine> currentCart = new ArrayList<>();
    private GridPane menuGrid;
    private VBox cartBox;
    private Label cartTotalLabel;
    private ListView<String> myOrdersList; // FIX: make it a field

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        seedMenu();

        // Gradient background for all scenes
        Background colorfulBG = new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#fcf6ba")), new Stop(0.5, Color.web("#a18cd1")), new Stop(1, Color.web("#fbc2eb"))),
                CornerRadii.EMPTY, Insets.EMPTY
        ));

        // Welcome Scene
        VBox welcomePane = new VBox(18);
        welcomePane.setAlignment(Pos.CENTER);
        welcomePane.setPadding(new Insets(20));
        welcomePane.setBackground(colorfulBG);
        Label title = new Label("University Cafeteria System");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 34));
        title.setTextFill(Color.web("#6a1b9a"));
        title.setEffect(new DropShadow(10, Color.web("#ba68c8")));
        Button studentBtn = styledBtn("Student", "#43a047");
        Button adminBtn = styledBtn("Admin", "#fbc02d", "#6d4c41");
        welcomePane.getChildren().addAll(title, studentBtn, adminBtn);
        welcomeScene = new Scene(welcomePane, 540, 430);

        // STUDENT LOGIN/REGISTER SCENE
        VBox stuLoginPane = new VBox(14);
        stuLoginPane.setAlignment(Pos.CENTER);
        stuLoginPane.setPadding(new Insets(35));
        stuLoginPane.setBackground(colorfulBG);
        Label loginTitle = new Label("Student Login / Register");
        loginTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        TextField idField = new TextField(); idField.setPromptText("Student ID");
        PasswordField passField = new PasswordField(); passField.setPromptText("Password");
        Button loginBtn = styledBtn("Login", "#1976d2");
        Button regBtn = styledBtn("Register", "#43a047");
        Label msg = new Label("");
        msg.setTextFill(Color.web("#d84315"));
        Button backBtn = styledBtn("Back", "#90caf9");
        backBtn.setOnAction(e -> mainStage.setScene(welcomeScene));

        stuLoginPane.getChildren().addAll(loginTitle, idField, passField, loginBtn, regBtn, msg, backBtn);
        studentLoginScene = new Scene(stuLoginPane, 400, 350);

        // STUDENT MAIN SCENE
        BorderPane stuMainPane = new BorderPane();
        stuMainPane.setPadding(new Insets(12));
        stuMainPane.setBackground(colorfulBG);

        // Header
        HBox userBar = new HBox(25);
        userBar.setAlignment(Pos.CENTER_LEFT);
        userBar.setPadding(new Insets(0,10,15,10));
        Label stuName = new Label("Welcome, Student");
        stuName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 19));
        stuName.setTextFill(Color.web("#6a1b9a"));
        Label stuPoints = new Label("Points: 0");
        stuPoints.setFont(Font.font("Segoe UI", 16));
        stuPoints.setTextFill(Color.web("#388e3c"));

        Button logoutBtn = styledBtn("Logout", "#f06292");
        logoutBtn.setOnAction(e -> { loggedInStudent = null; mainStage.setScene(welcomeScene); });
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        userBar.getChildren().addAll(stuName, stuPoints, spacer, logoutBtn);
        stuMainPane.setTop(userBar);

        // Student Tabs
        TabPane stuTabs = new TabPane();
        stuTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // -- 1. Menu Tab --
        Tab menuTab = new Tab("Menu");
        BorderPane menuTabPane = new BorderPane();
        menuTabPane.setBackground(new Background(new BackgroundFill(
                Color.web("#e1f5fe", 0.7), new CornerRadii(18), Insets.EMPTY)));
        menuTabPane.setPadding(new Insets(14,8,8,8));
        menuGrid = new GridPane();
        menuGrid.setHgap(22); menuGrid.setVgap(22); menuGrid.setAlignment(Pos.TOP_CENTER);
        ScrollPane menuScroll = new ScrollPane(menuGrid);
        menuScroll.setFitToWidth(true);
        menuScroll.setStyle("-fx-background-color:transparent;");
        menuTabPane.setCenter(menuScroll);

        // Cart box on the right
        cartBox = new VBox(10);
        cartBox.setPadding(new Insets(14));
        cartBox.setMinWidth(260);
        cartBox.setBackground(new Background(new BackgroundFill(Color.web("#fff8e1cc"), new CornerRadii(12), Insets.EMPTY)));
        Label cartTitle = new Label("🛒 Cart");
        cartTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        cartTitle.setTextFill(Color.web("#8e24aa"));
        cartTotalLabel = new Label("Total: EGP 0.00");
        cartTotalLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        cartTotalLabel.setTextFill(Color.web("#388e3c"));
        Button cartClear = styledBtn("Clear", "#e57373");
        cartClear.setOnAction(e -> { currentCart.clear(); refreshCart(); });
        Button cartPlace = styledBtn("Place Order", "#43a047");
        cartPlace.setOnAction(e -> {
            if (currentCart.isEmpty()) return;
            Order o = orders.placeOrder(loggedInStudent.getStudentId(), currentCart, new CashPaymentProcessor());
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Order "+o.getOrderId()+" placed. You earned "+o.getPointsEarned()+" pts.");
            a.showAndWait();
            currentCart.clear();
            refreshCart();
            updateStudentUI(stuName, stuPoints, myOrdersList);
        });
        cartBox.getChildren().addAll(cartTitle, cartTotalLabel, cartClear, cartPlace);

        HBox menuContent = new HBox(30, menuTabPane, cartBox);
        menuContent.setAlignment(Pos.TOP_CENTER);
        menuTab.setContent(menuContent);

        // -- 2. Redeem Points Tab --
        Tab redeemTab = new Tab("Redeem");
        VBox redeemBox = new VBox(18);
        redeemBox.setPadding(new Insets(30));
        redeemBox.setBackground(new Background(new BackgroundFill(Color.web("#f3e5f5", 0.7), new CornerRadii(18), Insets.EMPTY)));
        Label redeemInfo = new Label("🎁 50 pts → EGP 10 discount\n☕ 100 pts → Free Coffee");
        redeemInfo.setFont(Font.font("Segoe UI", 17));
        redeemInfo.setTextFill(Color.web("#5c6bc0"));
        Button redeem1 = styledBtn("Redeem Discount", "#43a047");
        Button redeem2 = styledBtn("Redeem Coffee", "#1976d2");
        Label redeemMsg = new Label();
        redeemMsg.setFont(Font.font("Segoe UI", 15));
        redeemMsg.setTextFill(Color.web("#d84315"));
        redeemBox.getChildren().addAll(redeemInfo, redeem1, redeem2, redeemMsg);
        redeemTab.setContent(redeemBox);

        // -- 3. My Orders Tab --
        Tab myOrdersTab = new Tab("My Orders");
        VBox myOrdersBox = new VBox(12);
        myOrdersBox.setPadding(new Insets(25,10,10,10));
        myOrdersList = new ListView<>(); // FIX: assign to field
        myOrdersBox.getChildren().add(myOrdersList);
        myOrdersTab.setContent(myOrdersBox);

        stuTabs.getTabs().addAll(menuTab, redeemTab, myOrdersTab);

        stuMainPane.setCenter(stuTabs);
        studentMainScene = new Scene(stuMainPane, 1000, 650);

        // ADMIN LOGIN SCENE
        VBox adminLoginPane = new VBox(18);
        adminLoginPane.setAlignment(Pos.CENTER);
        adminLoginPane.setPadding(new Insets(45));
        adminLoginPane.setBackground(colorfulBG);
        Label adminTitle = new Label("Admin Login");
        adminTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        TextField adminUser = new TextField(); adminUser.setPromptText("Admin Username");
        PasswordField adminPass = new PasswordField(); adminPass.setPromptText("Admin Password");
        Button adminLoginBtn = styledBtn("Login", "#fbc02d", "#6d4c41");
        Label adminMsg = new Label("");
        adminMsg.setTextFill(Color.web("#d84315"));
        Button backBtn2 = styledBtn("Back", "#90caf9");
        backBtn2.setOnAction(e -> mainStage.setScene(welcomeScene));

        adminLoginPane.getChildren().addAll(adminTitle, adminUser, adminPass, adminLoginBtn, adminMsg, backBtn2);

        adminLoginScene = new Scene(adminLoginPane, 400, 300);

        // ADMIN MAIN SCENE
        TabPane adminTabs = new TabPane();
        adminTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // -- 1. Menu CRUD Tab --
        Tab menuCrudTab = new Tab("Menu CRUD");
        VBox menuCrudBox = new VBox(12);
        menuCrudBox.setPadding(new Insets(24));
        menuCrudBox.setBackground(new Background(new BackgroundFill(
                Color.web("#e1bee7", 0.7), new CornerRadii(18), Insets.EMPTY)));
        ListView<String> adminMenuList = new ListView<>();
        adminMenuList.setPrefHeight(220);
        Button addMenuBtn = styledBtn("Add Item", "#43a047");
        Button editMenuBtn = styledBtn("Edit Selected", "#1976d2");
        Button delMenuBtn = styledBtn("Remove Selected", "#e53935");
        menuCrudBox.getChildren().addAll(adminMenuList, addMenuBtn, editMenuBtn, delMenuBtn);
        menuCrudTab.setContent(menuCrudBox);

        // -- 2. Orders Tab --
        Tab adminOrdersTab = new Tab("Pending Orders");
        VBox adminOrdersBox = new VBox(12);
        adminOrdersBox.setPadding(new Insets(24));
        adminOrdersBox.setBackground(new Background(new BackgroundFill(
                Color.web("#ffe0b2", 0.7), new CornerRadii(18), Insets.EMPTY)));
        ListView<String> adminOrderList = new ListView<>();
        adminOrderList.setPrefHeight(220);
        Button markPreparing = styledBtn("Set Preparing", "#ffb300");
        Button markReady = styledBtn("Set Ready", "#43a047");
        adminOrdersBox.getChildren().addAll(adminOrderList, markPreparing, markReady);
        adminOrdersTab.setContent(adminOrdersBox);

        // -- 3. Reports Tab --
        Tab adminReportTab = new Tab("Reports");
        VBox reportBox = new VBox(12);
        reportBox.setPadding(new Insets(24));
        reportBox.setBackground(new Background(new BackgroundFill(
                Color.web("#b3e5fc", 0.7), new CornerRadii(18), Insets.EMPTY)));
        Button dailyBtn = styledBtn("Daily Summary", "#1976d2");
        Button weeklyBtn = styledBtn("Weekly Summary", "#8d6e63");
        Button exportBtn = styledBtn("Export CSV", "#43a047");
        Label reportLabel = new Label();
        reportLabel.setFont(Font.font("Segoe UI", 15));
        reportBox.getChildren().addAll(dailyBtn, weeklyBtn, exportBtn, reportLabel);
        adminReportTab.setContent(reportBox);

        adminTabs.getTabs().addAll(menuCrudTab, adminOrdersTab, adminReportTab);
        BorderPane adminMainPane = new BorderPane();

// Header with label + logout
        HBox adminHeader = new HBox(20);
        adminHeader.setAlignment(Pos.CENTER_LEFT);
        adminHeader.setPadding(new Insets(10, 10, 10, 10));

        Label adminPanelLabel = new Label("Admin Panel");
        adminPanelLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        adminPanelLabel.setTextFill(Color.web("#6d4c41"));

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Button adminLogout = styledBtn("Logout", "#f06292");
        adminLogout.setOnAction(e -> mainStage.setScene(welcomeScene));

        adminHeader.getChildren().addAll(adminPanelLabel, spacer2, adminLogout);

        adminMainPane.setTop(adminHeader);
        adminMainPane.setCenter(adminTabs);
        adminMainPane.setBackground(colorfulBG);

        adminMainScene = new Scene(adminMainPane, 800, 650);

        // ---------- EVENT HANDLING ----------

        // Welcome
        studentBtn.setOnAction(e -> mainStage.setScene(studentLoginScene));
        adminBtn.setOnAction(e -> mainStage.setScene(adminLoginScene));

        // Student login/register
        loginBtn.setOnAction(e -> {
            Optional<Student> s = studentMgr.login(idField.getText().trim(), passField.getText().trim());
            if (s.isEmpty()) {
                msg.setText("Login failed.");
                return;
            }
            loggedInStudent = s.get();
            updateStudentUI(stuName, stuPoints, myOrdersList);
            refreshMenuGrid();
            refreshCart();
            mainStage.setScene(studentMainScene);
            msg.setText(""); idField.setText(""); passField.setText("");
        });

        regBtn.setOnAction(e -> {
            try {
                studentMgr.register("Student", idField.getText().trim(), passField.getText().trim());
                msg.setText("Registered! Please login.");
            } catch (Exception ex) {
                msg.setText(ex.getMessage());
            }
        });

        // Redeem
        redeem1.setOnAction(e -> {
            boolean ok = loyalty.redeemDiscount(loggedInStudent.getStudentId(), 50, 10.0);
            redeemMsg.setText(ok ? "Discount added!" : "Not enough points.");
            updateStudentUI(stuName, stuPoints, myOrdersList);
        });
        redeem2.setOnAction(e -> {
            boolean ok = loyalty.redeemFreeItem(loggedInStudent.getStudentId(), 100, "D001");
            redeemMsg.setText(ok ? "Free coffee token ready!" : "Not enough points.");
            updateStudentUI(stuName, stuPoints, myOrdersList);
        });

        // Refresh my orders
        stuTabs.getSelectionModel().selectedItemProperty().addListener((obs, old, tab) -> {
            if (tab == myOrdersTab) {
                myOrdersList.getItems().clear();
                orders.ordersOf(loggedInStudent.getStudentId()).forEach(o -> myOrdersList.getItems().add(o.toString()));
            }
        });

        // Admin login
        adminLoginBtn.setOnAction(e -> {
            if (adminUser.getText().equals("admin") && adminPass.getText().equals("admin123")) {
                mainStage.setScene(adminMainScene);
                adminMsg.setText(""); adminUser.setText(""); adminPass.setText("");
                refreshAdminMenu(adminMenuList);
                refreshAdminOrders(adminOrderList);
            } else {
                adminMsg.setText("Denied.");
            }
        });

        // Admin menu CRUD
        addMenuBtn.setOnAction(e -> {
            TextInputDialog d = new TextInputDialog();
            d.setHeaderText("Add Menu Item: id|name|desc|price|category");
            Optional<String> res = d.showAndWait();
            res.ifPresent(str -> {
                try {
                    String[] arr = str.split("\\|");
                    menuMgr.addItem(new MenuItem(arr[0], arr[1], arr[2], Double.parseDouble(arr[3]), arr[4]));
                    refreshAdminMenu(adminMenuList);
                    refreshMenuGrid();
                } catch (Exception ex) {}
            });
        });
        editMenuBtn.setOnAction(e -> {
            String sel = adminMenuList.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            String id = sel.split(" ")[0];
            menuMgr.findById(id).ifPresent(item -> {
                TextInputDialog d = new TextInputDialog(item.getName()+"|"+item.getPrice());
                d.setHeaderText("Edit: name|price");
                d.showAndWait().ifPresent(str -> {
                    String[] arr = str.split("\\|");
                    if (arr.length >= 1 && !arr[0].isEmpty()) item.setName(arr[0]);
                    if (arr.length >= 2 && !arr[1].isEmpty()) item.setPrice(Double.parseDouble(arr[1]));
                    refreshAdminMenu(adminMenuList);
                    refreshMenuGrid();
                });
            });
        });
        delMenuBtn.setOnAction(e -> {
            String sel = adminMenuList.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            String id = sel.split(" ")[0];
            menuMgr.removeItem(id); refreshAdminMenu(adminMenuList); refreshMenuGrid();
        });

        // Admin orders
        markPreparing.setOnAction(e -> {
            String sel = adminOrderList.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            String id = sel.split(" ")[1];
            if (sel.startsWith("[PLACED]")) {
                orderRepo.updateStatus(id, OrderStatus.PREPARING); // persist change
                refreshAdminOrders(adminOrderList);
            }
        });

        markReady.setOnAction(e -> {
            String sel = adminOrderList.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            String id = sel.split(" ")[1];
            if (sel.startsWith("[PREPARING]")) {
                orderRepo.delete(id); // completely remove
                refreshAdminOrders(adminOrderList);
            }
        });

        // Admin reports
        dailyBtn.setOnAction(e -> reportLabel.setText(reports.dailySummary()));
        weeklyBtn.setOnAction(e -> reportLabel.setText(reports.weeklySummary()));
        exportBtn.setOnAction(e -> reportLabel.setText("Exported: "+reports.exportCsv("reports")));

        // Start app
        mainStage.setScene(welcomeScene);
        mainStage.setTitle("Cafeteria System - Modern GUI");
        mainStage.show();
    }

    private void seedMenu() {
        menuMgr.addItem(new MenuItem("M001", "Chicken Shawarma", "Grilled chicken wrap", 75.0, "Main Course"));
        menuMgr.addItem(new MenuItem("M002", "Koshari Bowl", "Classic Egyptian mix", 55.0, "Main Course"));
        menuMgr.addItem(new MenuItem("D001", "Iced Coffee", "Cold brew", 35.0, "Drink"));
        menuMgr.addItem(new MenuItem("S001", "Chocolate Muffin", "Freshly baked", 20.0, "Snack"));
        menuMgr.addItem(new MenuItem("D002", "Fresh Lemonade", "Refreshing citrus", 28.0, "Drink"));
        menuMgr.addItem(new MenuItem("S002", "Chips", "Potato chips", 13.0, "Snack"));
        menuMgr.addItem(new MenuItem("M003", "Burger", "Juicy beef burger", 80.0, "Main Course"));
        menuMgr.addItem(new MenuItem("S003", "Brownie", "Chocolate fudge", 25.0, "Snack"));
    }

    // Styled button helper
    private Button styledBtn(String text, String bg) {
        return styledBtn(text, bg, "#fff");
    }
    private Button styledBtn(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-text-fill: " + fg + ";" +
                        "-fx-font-size: 16px;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 6 22 6 22;"
        );
        btn.setEffect(new DropShadow(3, Color.web("#bdbdbd44")));
        return btn;
    }

    // Refresh menu cards in columns (with animation)
    private void refreshMenuGrid() {
        menuGrid.getChildren().clear();
        int col = 0, row = 0, colsCount = 3;
        for (MenuItem mi : menuMgr.getMenu().values()) {
            StackPane card = createMenuCard(mi);
            menuGrid.add(card, col, row);
            animateCard(card, 130*row + 90*col); // Animation on load
            col++;
            if (col == colsCount) { col = 0; row++; }
        }
    }

    // Menu card design + add to cart
    private StackPane createMenuCard(MenuItem mi) {
        StackPane card = new StackPane();
        card.setPrefSize(230, 180);

        // Gradient for card background
        Paint cardBg = switch (mi.getCategory()) {
            case "Main Course" -> new LinearGradient(0,0,1,1,true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#fceabb")), new Stop(1, Color.web("#f8b500")));
            case "Drink" -> new LinearGradient(0,0,1,1,true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#a8edea")), new Stop(1, Color.web("#fed6e3")));
            case "Snack" -> new LinearGradient(0,0,1,1,true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#e0c3fc")), new Stop(1, Color.web("#8ec5fc")));
            default -> Color.web("#fff");
        };

        VBox box = new VBox(7);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10,10,10,10));
        box.setBackground(new Background(new BackgroundFill(cardBg, new CornerRadii(18), Insets.EMPTY)));
        box.setBorder(new Border(new BorderStroke(Color.web("#bdbdbd44"), BorderStrokeStyle.SOLID, new CornerRadii(18), BorderWidths.DEFAULT)));
        box.setEffect(new DropShadow(7, Color.web("#6d4c41aa")));
        box.setMinHeight(155);

        // icon (category based, use online icons or your local)
        ImageView icon = new ImageView(new Image("https://img.icons8.com/ios-filled/80/"+switch (mi.getCategory()){
            case "Drink" -> "coffee";
            case "Snack" -> "cookie";
            default -> "sandwich";
        }+".png"));
        icon.setFitHeight(42); icon.setFitWidth(42);

        Label name = new Label(mi.getName());
        name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 19));
        name.setTextFill(Color.web("#222"));

        Label desc = new Label(mi.getDescription());
        desc.setFont(Font.font("Segoe UI", 13));
        desc.setTextFill(Color.web("#616161"));
        desc.setWrapText(true);

        Label price = new Label("EGP " + mi.getPrice());
        price.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        price.setTextFill(Color.web("#00796b"));

        Button addBtn = styledBtn("Add to Cart", "#7e57c2");
        addBtn.setOnAction(e -> {
            currentCart.add(new OrderLine(mi, 1));
            addToCartAnimation(mi.getName());
            refreshCart();
        });

        box.getChildren().addAll(icon, name, desc, price, addBtn);
        card.getChildren().add(box);
        card.setOpacity(0); // For animation
        card.setScaleX(0.85); card.setScaleY(0.85);

        return card;
    }

    private void animateCard(StackPane card, int delayMs) {
        FadeTransition fade = new FadeTransition(Duration.millis(600), card);
        fade.setFromValue(0); fade.setToValue(1);
        ScaleTransition scale = new ScaleTransition(Duration.millis(600), card);
        scale.setFromX(0.85); scale.setToX(1.0);
        scale.setFromY(0.85); scale.setToY(1.0);
        fade.setDelay(Duration.millis(delayMs));
        scale.setDelay(Duration.millis(delayMs));
        fade.play();
        scale.play();
    }

    private void addToCartAnimation(String item) {
        Label added = new Label("✓ " + item + " added!");
        added.setFont(Font.font("Segoe UI Semibold", 15));
        added.setTextFill(Color.web("#43a047"));
        cartBox.getChildren().add(1, added);
        FadeTransition ft = new FadeTransition(Duration.millis(900), added);
        ft.setFromValue(1); ft.setToValue(0);
        ft.setOnFinished(ev -> cartBox.getChildren().remove(added));
        ft.play();
    }

    private void refreshCart() {
        // Remove old cart items (keep title, total, clear, place)
        cartBox.getChildren().removeIf(node -> node instanceof Label && node != cartTotalLabel && !((Label) node).getText().startsWith("Total"));
        double total = 0.0;
        for (OrderLine ol : currentCart) {
            Label line = new Label(ol.getItem().getName() + " x" + ol.getQuantity() + " - EGP " + ol.lineTotal());
            line.setFont(Font.font("Segoe UI", 14));
            line.setTextFill(Color.web("#333"));
            line.setStyle("-fx-background-color: #fffde7cc; -fx-background-radius: 10;");
            line.setPadding(new Insets(7,7,7,7));
            cartBox.getChildren().add(cartBox.getChildren().size()-2, line);
            total += ol.lineTotal();
        }
        cartTotalLabel.setText(String.format("Total: EGP %.2f", total));
    }

    private void updateStudentUI(Label name, Label points, ListView<String> myOrders) {
        name.setText("Welcome, " + loggedInStudent.getName());
        points.setText("Points: " + loggedInStudent.getPoints());
        // orders
        myOrders.getItems().clear();
        orders.ordersOf(loggedInStudent.getStudentId()).forEach(o -> myOrders.getItems().add(o.toString()));
    }

    private void refreshAdminMenu(ListView<String> list) {
        list.getItems().clear();
        menuMgr.getMenu().values().stream()
                .sorted(Comparator.comparing(MenuItem::getId))
                .forEach(item -> list.getItems().add(item.getId() + " | " + item.getName() + " - " + item.getPrice() + " [" + item.getCategory() + "]"));
    }

    private void refreshAdminOrders(ListView<String> adminOrderList) {
        adminOrderList.getItems().setAll(
                orderRepo.all().stream()
                        .filter(o -> o.getStatus() != OrderStatus.READY_FOR_PICKUP) // hide ready
                        .map(o -> "[" + o.getStatus() + "] " + o.getOrderId() + " - " + o.getStudentId())
                        .toList()
        );
    }


    public static void main(String[] args) {
        launch(args);
    }
}