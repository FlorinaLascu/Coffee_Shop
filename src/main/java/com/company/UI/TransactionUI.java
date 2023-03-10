package com.company.UI;

import com.company.connection.DbAdapter;
import com.company.frame.MyTable;
import com.company.frame.QueryItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class TransactionUI extends JFrame {
    private final int COLUMN = 2;
    private final List<String> TITLES = Arrays.asList(
            "TRANSACTION_ID", "CUSTOMER_ID");
    private Vector<Vector<String>> dataModel = new Vector<Vector<String>>();
    private QueryItem customerID = new QueryItem("TRANSACTION_ID：", 10);
    private QueryItem firstName = new QueryItem("CUSTOMER_ID：", 10);
    private JButton queryBtn = new JButton("SEARCH");
    private JButton saveBtn = new JButton("UPDATE");
    private JButton insertBtn = new JButton("INSERT");
    private JButton deleteBtn = new JButton("DELETE");
    private JTextArea textarea = new JTextArea(5, 5);
    private MyTable table;

    private static DbAdapter dbAdapter;


    public TransactionUI(DbAdapter adapter) {
        this.setTitle("Transaction");
        dbAdapter = adapter;

        Vector<String> titles = new Vector<String>(TITLES);
        table = new MyTable(dataModel, titles);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(customerID);
        controlPanel.add(firstName);
        controlPanel.add(Box.createRigidArea(new Dimension(1000,0)));
        controlPanel.add(queryBtn);
        //controlPanel.add(saveBtn);
        controlPanel.add(insertBtn);
        controlPanel.add(deleteBtn);
        controlPanel.setPreferredSize(new Dimension(0, 130));

        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        tablePanel.add(table.getTableHeader());
        tablePanel.add(new JScrollPane(table));

        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());
        container.add(textarea, BorderLayout.NORTH);
        container.add(tablePanel, BorderLayout.CENTER);

        this.add(controlPanel, BorderLayout.NORTH);
        this.add(container, BorderLayout.CENTER);
        this.add(Box.createRigidArea(new Dimension(20, 0)), BorderLayout.WEST);
        this.add(Box.createRigidArea(new Dimension(20, 0)), BorderLayout.EAST);
        this.add(Box.createRigidArea(new Dimension(0, 20)), BorderLayout.SOUTH);

        setActionListener();
    }

    private void setActionListener() {
        queryBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> conditions = new ArrayList<String>();
                if (customerID.isSelected()) conditions.add("(TRANSACTION_ID = " + customerID.getText() + ")");
                if (firstName.isSelected()) conditions.add("(CUSTOMER_ID = '" + firstName.getText() + "')");

                StringBuilder sb = new StringBuilder();
                sb.append("SELECT * FROM \"TRANSACTION\"");
                int length = conditions.size();
                if (length != 0) sb.append(" where ");
                for (int i = 0; i < length; i++) {
                    sb.append(conditions.get(i));
                    if (i != length - 1) sb.append(" AND ");
                }
                sb.append(";");
                String queryString = sb.toString();
                textarea.setText(queryString);

                dataModel.clear();
                Statement stmt;
                try {
                    stmt = dbAdapter.getConnection().createStatement();
                    ResultSet rs = stmt.executeQuery(queryString);
                    Vector<String> record;
                    while (rs.next()) {
                        record = new Vector<String>();
                        for (int i = 0; i < COLUMN; i++) {
                            record.add(rs.getString(i + 1));
                        }
                        dataModel.add(record);
                    }
                    stmt.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }

                table.validate();
                table.updateUI();
            }
        });


        insertBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String id = customerID.getText();
                String sname = firstName.getText();


                String cmd = "INSERT INTO \"TRANSACTION\" VALUES (" + id + ", '" + sname + "');";
                textarea.setText(cmd);

                try {
                    Statement statement = dbAdapter.getConnection().createStatement();
                    String sqlInsert = "INSERT INTO \"TRANSACTION\" (\"TRANSACTION_ID\", \"CUSTOMER_ID\") "+
                            "VALUES (" + id + ", '" + sname + "');";
                    System.out.println(sqlInsert);
                    statement.executeUpdate(sqlInsert);
                    statement.close();
                    dataModel.add(new Vector<String>(Arrays.asList(
                            id, sname)));


                    table.validate();
                    table.updateUI();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }

        });

        deleteBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                String sid = dataModel.get(row).get(0);
                String sql = "DELETE FROM \"TRANSACTION\" WHERE \"CUSTOMER_ID\" = '" + sid + "';";


                textarea.setText(sql);

                try {
                    Statement statement = dbAdapter.getConnection().createStatement();
                    String sqlDelete = "DELETE FROM \"TRANSACTION\" WHERE \"CUSTOMER_ID\" = '" + sid + "';";
                    statement.executeUpdate(sqlDelete);
                    ((DefaultTableModel)table.getModel()).removeRow(row);
                    statement.close();


                    table.validate();
                    table.updateUI();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
}
