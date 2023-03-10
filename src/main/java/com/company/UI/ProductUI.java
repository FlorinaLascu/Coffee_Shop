package com.company.UI;

import com.company.connection.DbAdapter;
import com.company.frame.MyTable;
import com.company.frame.QueryItem;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ProductUI extends JFrame {
    private final int COLUMN = 5;
    private final List<String> TITLES = Arrays.asList(
            "PRODUCT_ID", "NAME", "COUNT", "PRICE", "CATEGORY_TYPE");
    private Vector<Vector<String>> dataModel = new Vector<Vector<String>>();
    private QueryItem productID = new QueryItem("PRODUCT_ID：", 10);
    private QueryItem name = new QueryItem("NAME：", 10);
    private QueryItem count = new QueryItem("COUNT：", 10);
    private QueryItem price = new QueryItem("PRICE：", 22);
    private QueryItem categoryType = new QueryItem("CATEGORY_TYPE：", 5);
    private JButton queryBtn = new JButton("SEARCH");
    private JButton saveBtn = new JButton("UPDATE");
    private JButton insertBtn = new JButton("INSERT");
    private JButton deleteBtn = new JButton("DELETE");
    private JTextArea textarea = new JTextArea(5, 5);
    private MyTable table;

    private static DbAdapter dbAdapter;

    //构造函数，负责创建用户界面
    public ProductUI(DbAdapter adapter) {
        this.setTitle("Product");
        dbAdapter = adapter;

        Vector<String> titles = new Vector<String>(TITLES);
        table = new MyTable(dataModel, titles);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        //table.getColumnModel().getColumn(4).setPreferredWidth(150);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(productID);
        controlPanel.add(name);
        controlPanel.add(count);
        controlPanel.add(price);
        controlPanel.add(categoryType);
        controlPanel.add(Box.createRigidArea(new Dimension(100,0)));
        //controlPanel.add(avgPoint);
        controlPanel.add(queryBtn);
        controlPanel.add(saveBtn);
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
        //根据指定条件，列出数据库中满足条件的记录
        queryBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> conditions = new ArrayList<String>();
                if (productID.isSelected()) conditions.add("(PRODUCT_ID = " + productID.getText() + ")");
                if (name.isSelected()) conditions.add("(NAME = '" + name.getText() + "')");
                if (count.isSelected()) conditions.add("(COUNT = " + count.getText() + ")");
                if (price.isSelected()) conditions.add("(PRICE = " + price.getText() + ")");
                if (categoryType.isSelected()) conditions.add("(CATEGORY_TYPE = '" + categoryType.getText() + "')");
                //if (avgPoint.isSelected()) conditions.add("(AVG_POINTS = " + avgPoint.getText() + ")");

                StringBuilder sb = new StringBuilder();
                sb.append("SELECT * FROM \"PRODUCT\"");
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
                //更新表格
                table.validate();
                table.updateUI();
            }
        });

        //往数据库中插入一条新的记录
        insertBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String id = productID.getText();
                String sname = name.getText();
                String ssex = count.getText();
                String sclass = price.getText();
                String sdept = categoryType.getText();
                //String saddr = avgPoint.getText();

                //在文本框显示 SQL 命令
                String cmd = "INSERT INTO \"PRODUCT\" VALUES (" + id + ", '" + sname + "', " +
                        ssex + ", " + sclass + ", '" + sdept + "');";
                textarea.setText(cmd);

                try {
                    Statement statement = dbAdapter.getConnection().createStatement();
                    String sqlInsert = "INSERT INTO \"PRODUCT\" (\"PRODUCT_ID\", \"NAME\", \"COUNT\", \"PRICE\", \"CATEGORY_TYPE\") "+
                            "VALUES (" + id + ", '" + sname + "', " + ssex + ", " + sclass + ", '" + sdept +"');";
                    System.out.println(sqlInsert);
                    statement.executeUpdate(sqlInsert);
                    statement.close();
                    dataModel.add(new Vector<String>(Arrays.asList(
                            id, sname, ssex, sclass, sdept)));

                    //更新表格
                    table.validate();
                    table.updateUI();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }

        });

        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                String sid = dataModel.get(row).get(0);
                ArrayList<String> conditions = new ArrayList<String>();
                if (name.isSelected())
                {
                    table.setValueAt(name.getText(), row, 1);
                    conditions.add("name = '" + name.getText() + "'");
                }
                if (count.isSelected())
                {
                    table.setValueAt(count.getText(), row, 2);
                    conditions.add("count = " + count.getText() + "");
                }
                if (price.isSelected())
                {
                    table.setValueAt(price.getText(), row, 3);
                    conditions.add("price = " + price.getText() + "");
                }
                if (categoryType.isSelected())
                {
                    table.setValueAt(categoryType.getText(), row, 4);
                    conditions.add("category_type = '" + categoryType.getText() + "'");
                }

                if(conditions.size() == 0) return;

                StringBuilder sb = new StringBuilder();
                sb.append("UPDATE \"PRODUCT\" SET ");
                for(int i = 0; i < conditions.size(); i++)
                {
                    sb.append(conditions.get(i));
                    if(i != conditions.size() - 1)
                        sb.append(", ");
                }
                sb.append(" WHERE \"PRODUCT_ID\" = " + sid + ";");
                String queryString = sb.toString();
                textarea.setText(queryString);

                Statement stmt;
                try {
                    stmt = dbAdapter.getConnection().createStatement();
                    stmt.executeUpdate(queryString);
                    stmt.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                //更新表格
                table.validate();
                table.updateUI();
            }
        });

        //将用户当前选中的记录从数据库中删除
        deleteBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                String sid = dataModel.get(row).get(0);
                String sql = "DELETE FROM \"PRODUCT\" WHERE \"PRODUCT_ID\" = " + sid + ";";

                //在文本框显示 SQL 命令
                textarea.setText(sql);

                try {
                    Statement statement = dbAdapter.getConnection().createStatement();
                    String sqlDelete = "DELETE FROM \"PRODUCT\" WHERE \"PRODUCT_ID\" = " + sid + ";";
                    statement.executeUpdate(sqlDelete);
                    ((DefaultTableModel)table.getModel()).removeRow(row);
                    statement.close();

                    //更新表格
                    table.validate();
                    table.updateUI();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
}




