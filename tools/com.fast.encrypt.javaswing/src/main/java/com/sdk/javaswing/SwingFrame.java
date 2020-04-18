package com.sdk.javaswing;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

public class SwingFrame extends JFrame implements MouseListener, ActionListener {
    private static JTextArea input;
    private static JTextArea output;
    private JButton button;

    public SwingFrame() {
        super("Hello Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 750);
        setVisible(true);
        setResizable(false);

        setLayout(new FlowLayout());

        JScrollPane scrollPane_input = new JScrollPane();
        scrollPane_input.setPreferredSize(new Dimension(700, 300));
        add(scrollPane_input);
        scrollPane_input.setLocation(0, 200);

        input = new JTextArea();
        input.addMouseListener(this);
        input.setFont(new Font(null, 0, 16));
        scrollPane_input.setViewportView(input);


        button = new JButton("encrypt");
        button.setPreferredSize(new Dimension(150, 50));
        button.addActionListener(encryptAction);
//        button.setBounds(350, 350, 600, 400);
        add(button);

        JButton decryptBtn = new JButton("decrypt");
        decryptBtn.setPreferredSize(new Dimension(150, 50));
        decryptBtn.addActionListener(decryptAction);
//        button.setBounds(350, 350, 600, 400);
        add(decryptBtn);

        JScrollPane scrollPane_out = new JScrollPane();
        scrollPane_out.setPreferredSize(new Dimension(700, 300));
        add(scrollPane_out);

        output = new JTextArea();
        output.setFont(new Font(null, 0, 16));
        scrollPane_out.setViewportView(output);

        init();
    }

    private JMenuItem pasteMenu = new JMenuItem("paste");
    private JMenuItem copyMenu = new JMenuItem("copy");
    private JMenuItem cutMenu = new JMenuItem("cut");
    private JPopupMenu popupMenu = null;

    private void init() {
        popupMenu = new JPopupMenu();

        cutMenu.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_MASK));
        copyMenu.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_MASK));
        pasteMenu.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_MASK));

        cutMenu.addActionListener(this);
        copyMenu.addActionListener(this);
        pasteMenu.addActionListener(this);

        popupMenu.add(cutMenu);
        popupMenu.add(copyMenu);
        popupMenu.add(pasteMenu);
        input.add(popupMenu);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        action(actionEvent);
    }

    public void action(ActionEvent e) {
        String str = e.getActionCommand();
        if (str.equals(copyMenu.getText())) { // 复制
            input.copy();
        } else if (str.equals(pasteMenu.getText())) { // 粘贴
            input.paste();
        } else if (str.equals(cutMenu.getText())) { // 剪切
            input.cut();
        }
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        popupMenuTrigger(mouseEvent);
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        popupMenuTrigger(mouseEvent);
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    private void popupMenuTrigger(MouseEvent e) {
        if (e.isPopupTrigger()) {
            this.requestFocusInWindow();
            cutMenu.setEnabled(isAbleToCopyAndCut());
            copyMenu.setEnabled(isAbleToCopyAndCut());
            pasteMenu.setEnabled(isAbleToPaste());
            popupMenu.show(this, e.getX() + 3, e.getY() + 3);
        }
    }

    private boolean isAbleToCopyAndCut() {
        return (input.getSelectionStart() != input.getSelectionEnd());
    }

    private boolean isAbleToPaste() {
        Transferable content = this.getToolkit().getSystemClipboard().getContents(this);
        try {
            return (content.getTransferData(DataFlavor.stringFlavor) instanceof String);
        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ActionListener decryptAction = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent event) {
            // TODO Auto-generated method stub
            String inputText = input.getText();

            String decrpty = AESUtil.getInstance().decrypt(inputText);
            output.setText(decrpty);
//            txt.setText(((JButton) event.getSource()).getText());
        }
    };

    private ActionListener encryptAction = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent event) {
            // TODO Auto-generated method stub
            String inputText = input.getText();

            String encrpty = AESUtil.getInstance().encrypt(inputText);
            output.setText(encrpty);
        }
    };

}
