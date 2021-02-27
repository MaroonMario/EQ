package org.example.frame;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Stack;
import java.util.prefs.Preferences;

public class EQ extends Dialog {
    private JTextField ipEndTField;
    private JTextField ipStartTField;
    private JTextField userNameTField;
    private JPasswordField passwordTField;
    private JTextField placardPathTField;
    private JTextField updatePathTField;
    private JTextField pubPathTField;
    public static EQ frame = null;
    private ChatTree chatTree;
    private JPopupMenu popupMenu;
    private JTabbedPane tabbedPane;
    private JToggleButton searchUserButton;
    private JProgressBar progressBar;
    private JList faceList;
    private JButton selectInterfaceOKButton;
    private DatagramSocket ss;
    private final JLabel stateLabel;
    private static String user_dir;
    private static File localFile;
    private static File netFile;
    private String netFilePath;
    private JButton messageAlertButton;
    private Stack<String> messageStack;
    private ImageIcon messageAlertIcon;
    private ImageIcon messageAlertNullIcon;
    private Rectangle location;
    public static TrayIcon trayicon;
    private Dao dao;
    public final static Preferences preferences = Preferences.systemRoot();;
    private JButton userInfoButton;
    public static void main(String args[]) {
        try {
            String laf = preferences.get("lookAndFeel", "java默认");
            if (laf.indexOf("当前系统")>-1)
                UIManager.setLookAndFeel(UIManager
                        .getSystemLookAndFeelClassName());
            EQ frame = new EQ();
            frame.setVisible(true);
            frame.SystemTrayInitial();// 初始化系统栏
            frame.server();
            frame.checkPlacard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public EQ() {
        super(new Frame());
        frame = this;
        dao = Dao.getDao();
        location = dao.getLocation();
        setTitle("EQ通讯");
        setBounds(location);
        progressBar = new JProgressBar();
        progressBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        tabbedPane = new JTabbedPane();
        popupMenu = new JPopupMenu();
        chatTree = new ChatTree(this);
        user_dir = System.getProperty("user.dir");// 程序执行路径用于系统更新
        localFile = new File(user_dir + File.separator + "EQ.jar");// 本地EQ文件
        stateLabel = new JLabel();// 状态栏标签
        addWindowListener(new FrameWindowListener())// 添加窗体监视器
        addComponentListener(new ComponentAdapter() {
            public void componentResized(final ComponentEvent e) {
                saveLocation();
            }
            public void componentMoved(final ComponentEvent e) {
                saveLocation();
            }
        });
        try {// 启动通讯服务端口
            ss = new DatagramSocket(1111);
        } catch (SocketException e2) {
            if (e2.getMessage().startsWith("Address already in use"))
                showMessageDialog("服务端口被占用,或者本软件已经运行。");
            System.exit(0);
        }
        { // 初始化公共信息按钮
            messageAlertIcon = new ImageIcon(EQ.class
                    .getResource("/image/messageAlert.gif"));
            messageAlertNullIcon = new ImageIcon(EQ.class
                    .getResource("/image/messageAlertNull20.gif"));
            messageStack = new Stack<String>();
            messageAlertButton = new JButton();
            messageAlertButton.setHorizontalAlignment(SwingConstants.RIGHT);
            messageAlertButton.setContentAreaFilled(false);
            final JPanel BannerPanel = new JPanel();
            BannerPanel.setLayout(new BorderLayout());
            add(BannerPanel, BorderLayout.NORTH);
            userInfoButton = new JButton();
            BannerPanel.add(userInfoButton, BorderLayout.WEST);
            userInfoButton.setMargin(new Insets(0, 0, 0, 10));
            initUserInfoButton();// 初始化本地用户头像按钮
            BannerPanel.add(messageAlertButton, BorderLayout.CENTER);
            messageAlertButton.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    if (!messageStack.empty()) {
                        showMessageDialog(messageStack.pop());
                    }
                }
            });
            messageAlertButton.setIcon(messageAlertIcon);
            showMessageBar();
        }
        add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.setTabPlacement(SwingConstants.LEFT);
        ImageIcon userTicon = new ImageIcon(EQ.class
                .getResource("/image/tabIcon/tabLeft.PNG"));
        tabbedPane.addTab(null, userTicon, createUserList(), "用户列表");
        ImageIcon sysOTicon = new ImageIcon(EQ.class
                .getResource("/image/tabIcon/tabLeft2.PNG"));
        tabbedPane.addTab(null, sysOTicon, createSysToolPanel(), "系统操作");
        ImageIcon sysSTicon = new ImageIcon(EQ.class
                .getResource("/image/tabIcon/tabLeft3.png"));
        tabbedPane.addTab(null, sysSTicon, createSysSetPanel(), "系统设置");
        setAlwaysOnTop(true);
    }

    private void checkPlacard(){
        String placardDir = preferences.get("placarPath",null);
        if(placardDir == null){
            pushMessage("未设置公告路径");
            return;
        }
        File placard = new File(placardDir);
        try{
            if(placard.exists() && placard.isFile()){
                StringBuilder placardStr = new StringBuilder();
                Scanner sc = new Scanner(new FileInputStream(placard));
                while(sc.hasNextLine()){
                    placardStr.append(sc.nextLine());
                }
                pushMessage(placardStr.toString());
            }
        }catch (FileNotFoundException e){
            pushMessage("公告路径错误，或公告文件不存在。");
        }
    }

    private void initUserInfoButton(){
        try{
            String ip = InetAddress.getLocalHost().getHostAddress();
            User user = dao.getUser(ip);
            userInfoButton.setIcon(user.getIconImg());
            userInfoButton.setText(user.getName());
            userInfoButton.setIconTextGap(user.getTipText());
            userInfoButton.getToolTipText(user.getTipText());
            userInfoButton.getParent().doLayout();
        } catch (UnknownHostException e){
            e.printStackTrace();
        }
    }
    private void saveLocation() { // 保存主窗体位置的方法
        location = getBounds();
        dao.updateLocation(location);
    }
}
