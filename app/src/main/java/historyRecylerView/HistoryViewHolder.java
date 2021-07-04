package historyRecylerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ubersimcoder.HistorySingleActivity;
import com.example.ubersimcoder.R;

public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    public TextView rideId;
    public TextView time;

    public HistoryViewHolder(@NonNull View itemView)
    {
        super(itemView);

        itemView.setOnClickListener(this);

        rideId = itemView.findViewById(R.id.rideId);
        time = itemView.findViewById(R.id.time);
    }

    @Override
    public void onClick(View v)
    {
        Intent intent = new Intent(v.getContext(), HistorySingleActivity.class);

        Bundle b = new Bundle();
        b.putString("rideId",rideId.getText().toString());
        intent.putExtras(b);
        v.getContext().startActivity(intent);

    }
}
