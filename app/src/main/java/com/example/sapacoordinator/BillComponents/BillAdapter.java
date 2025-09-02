package com.example.sapacoordinator.BillComponents;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapacoordinator.R;
import com.example.sapacoordinator.SchoolComponents.ChooseAction;
import com.example.sapacoordinator.ViewBookingComponents.BookingPayment.FinalPayment;

import java.util.List;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.BillViewHolder> {

    private List<Bill> billList;
    private final Context context;
    private OnBillClickListener onBillClickListener;

    public interface OnBillClickListener {
        void onPayNowClick(Bill bill);
        void onViewReceiptClick(Bill bill);
    }

    public BillAdapter(List<Bill> billList, Context context) {
        this.billList = billList;
        this.context = context;
    }

    public void setOnBillClickListener(OnBillClickListener listener) {
        this.onBillClickListener = listener;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bill_card, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill bill = billList.get(position);

        // Set basic bill information
        holder.tvBillReference.setText(bill.getBillReference());
        holder.tvAppointmentId.setText(String.valueOf(bill.getAppointmentId()));
        holder.tvDateIssued.setText(bill.getDateIssued());
        holder.tvTotalAmount.setText(bill.getFormattedAmount());
        holder.tvStatus.setText(bill.getStatus());

        // Configure UI based on payment status
        if (bill.isPaid()) {
            // Paid bill styling
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary_light));
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            holder.tvStatus.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light));
            holder.tvTotalAmount.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));

            // Show paid date and hide pay button
            if (bill.getPaidDate() != null && !bill.getPaidDate().isEmpty()) {
                holder.tvPaidDate.setText(bill.getPaidDate());
                holder.llPaidDate.setVisibility(View.VISIBLE);
            }

            holder.btnAction.setText(context.getString(R.string.view_receipt));
            holder.btnAction.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.holo_green_dark));
            holder.btnAction.setOnClickListener(v -> {
                if (onBillClickListener != null) {
                    onBillClickListener.onViewReceiptClick(bill);
                }
            });
        } else {
            // Unpaid bill styling
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            holder.tvStatus.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
            holder.tvTotalAmount.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));

            // Hide paid date
            holder.llPaidDate.setVisibility(View.GONE);

            holder.btnAction.setText(context.getString(R.string.pay_now));
            holder.btnAction.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorPrimary));
            holder.btnAction.setOnClickListener(v -> {
                // Navigate to FinalPayment activity with bill data
                navigateToFinalPayment(bill);
            });
        }
    }

    private void navigateToFinalPayment(Bill bill) {
        Intent intent = new Intent(context, FinalPayment.class);
        intent.putExtra("appointment_id", bill.getAppointmentId());
        intent.putExtra("bill_id", bill.getBillId());
        intent.putExtra("bill_reference", bill.getBillReference());
        intent.putExtra("total_amount", bill.getTotalAmount());
        intent.putExtra("school_id", bill.getSchoolId());

        // Pass school data if available from context (ChooseAction activity)
        if (context instanceof ChooseAction) {
            ChooseAction chooseAction = (ChooseAction) context;
            // We need to get school data from the activity
            Intent currentIntent = chooseAction.getIntent();
            intent.putExtra("school_name", currentIntent.getStringExtra("school_name"));
            intent.putExtra("school_address", currentIntent.getStringExtra("school_address"));
            intent.putExtra("school_contact", currentIntent.getStringExtra("school_contact"));
        }

        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return billList != null ? billList.size() : 0;
    }

    public void updateBills(List<Bill> newBills) {
        this.billList = newBills;
        notifyDataSetChanged();
    }

    public static class BillViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvBillReference, tvStatus, tvAppointmentId, tvDateIssued,
                 tvTotalAmount, tvPaidDate, tvPaidDateLabel;
        LinearLayout llPaidDate;
        Button btnAction;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvBillReference = itemView.findViewById(R.id.tvBillReference);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAppointmentId = itemView.findViewById(R.id.tvAppointmentId);
            tvDateIssued = itemView.findViewById(R.id.tvDateIssued);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvPaidDate = itemView.findViewById(R.id.tvPaidDate);
            tvPaidDateLabel = itemView.findViewById(R.id.tvPaidDateLabel);
            llPaidDate = itemView.findViewById(R.id.llPaidDate);
            btnAction = itemView.findViewById(R.id.btnPayNow);
        }
    }
}
