package Dri_verse.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import static Dri_verse.view.InstructorPanels.*;

/** Learner's Payments tab: submit a payment to an instructor and see its status. */
public class LearnerPaymentsPanel extends Section {

    private final JComboBox<Student> instructorBox = styleField(new JComboBox<>());
    private final JSpinner amount = styleField(new JSpinner(new SpinnerNumberModel(500.0, 1.0, 200000.0, 100.0)));
    private final JComboBox<String> method =
            styleField(new JComboBox<>(new String[]{"UPI", "Cash", "Card", "Net Banking"}));
    private final JTextField reference = styleField(new JTextField(18));
    private final DefaultTableModel model =
            readOnly("Instructor", "Amount (\u20B9)", "Method", "Reference", "Date", "Status");

    public LearnerPaymentsPanel(int learnerId) {
        super(learnerId);
        JButton pay = pill("Submit Payment");
        pay.addActionListener(e -> submit());
        add(formCard("Make a Payment",
                labeled("Pay to (instructor)", instructorBox),
                labeled("Amount (\u20B9)", amount),
                labeled("Method", method),
                labeled("Reference / transaction id", reference),
                labeled(" ", pay)), BorderLayout.NORTH);
        add(tableCard("My Payments", null, styledTable(model), null), BorderLayout.CENTER);
        reload();
    }

    private void submit() {
        Student ins = (Student) instructorBox.getSelectedItem();
        if (ins == null) { info(this, "No instructor is assigned to you yet."); return; }
        double amt = ((Number) amount.getValue()).doubleValue();
        if (amt <= 0) { info(this, "Enter an amount greater than 0."); return; }
        String m = (String) method.getSelectedItem();
        String ref = reference.getText().trim();
        if (!"Cash".equals(m) && ref.isEmpty()) {
            info(this, "Please enter the transaction id / reference.");
            return;
        }
        try {
            Repo.submitPayment(userId, ins.id, amt, m, ref);
            reference.setText("");
            reload();
            info(this, "Payment submitted. Your instructor will confirm it shortly.");
        } catch (Exception e) { error(this, e); }
    }

    @Override public void reload() {
        try {
            Student keep = (Student) instructorBox.getSelectedItem();
            instructorBox.removeAllItems();
            List<Student> list = Repo.instructorsOf(userId);
            for (Student s : list) {
                instructorBox.addItem(s);
                if (keep != null && keep.id == s.id) instructorBox.setSelectedItem(s);
            }
            model.setRowCount(0);
            for (Object[] row : Repo.myPayments(userId)) model.addRow(row);
        } catch (Exception e) { error(this, e); }
    }
}