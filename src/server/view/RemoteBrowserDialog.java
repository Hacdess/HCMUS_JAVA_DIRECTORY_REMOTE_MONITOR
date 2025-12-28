package SRC.server.view;

import SRC.server.controller.ServerController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class RemoteBrowserDialog extends JDialog {
  private ServerController controller;
  private String clientName;
  private DefaultListModel<String> listModel = new DefaultListModel<>();
  private JList<String> listView = new JList<>(listModel);
  private JButton upButton = new JButton("Up");
  private JButton selectButton = new JButton("Select");
  private JButton refreshButton = new JButton("Refresh");
  private String currentPath = "";
  private String selectedPath = null;

  public RemoteBrowserDialog(Frame owner, ServerController controller, String clientName) {
    super(owner, "Browse: " + clientName, true);
    this.controller = controller;
    this.clientName = clientName;
    setSize(600, 400);
    setLayout(new BorderLayout());

    Panel top = new Panel(new FlowLayout(FlowLayout.LEFT));
    top.add(upButton);
    top.add(refreshButton);
    add(top, BorderLayout.NORTH);

    listView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scrollPane = new JScrollPane(listView);
    add(scrollPane, BorderLayout.CENTER);

    Panel bottom = new Panel(new FlowLayout(FlowLayout.RIGHT));
    bottom.add(selectButton);
    add(bottom, BorderLayout.SOUTH);

    refreshList();

    upButton.addActionListener(a -> {
      if (currentPath == null || currentPath.isEmpty()) return;
      java.io.File f = new java.io.File(currentPath);
      String parent = f.getParent();
      currentPath = (parent == null) ? "" : parent;
      refreshList();
    });

    refreshButton.addActionListener(a -> refreshList());

    listView.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          String sel = listView.getSelectedValue();
          if (sel == null) return;
          currentPath = sel;
          refreshList();
        }
      }
    });

    selectButton.addActionListener(a -> {
      selectedPath = listView.getSelectedValue();
      dispose();
    });
  }

  private void refreshList() {
    listModel.clear();
    List<String> children = controller.getClientChildren(clientName, currentPath);
    if (children.isEmpty()) {
      controller.sendListRequest(clientName, currentPath == null ? "" : currentPath);
      try { Thread.sleep(300); } catch (InterruptedException ignored) {}
      children = controller.getClientChildren(clientName, currentPath);
    }

    for (String c : children) listModel.addElement(c);
  }

  public String getSelectedPath() { return selectedPath; }
}
