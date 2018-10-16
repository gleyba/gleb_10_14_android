package test.gleb_10_14_android;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;


import java.util.Collections;
import java.util.ArrayList;

import test.gleb_10_14_android.databinding.RowItemBinding;

public class MainViewRowsAdapter
extends RecyclerView.Adapter<MainViewRowsAdapter.ItemViewHolder>
implements MainContract.View.Adapter
{
    private ArrayList<String> items = new ArrayList<>();

    public MainViewRowsAdapter() {}

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RowItemBinding binding = RowItemBinding.inflate(inflater, parent, false);
        return new ItemViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.binding.rowText.setText(items.get(position));
    }

    @Override
    public void addItems(ArrayList<String> data) {
        if (data.size() == 0) {
            return;
        }

        int prevSize = items.size();
        items.addAll(data);
        notifyItemRangeInserted(prevSize, items.size() - prevSize);

    }

    @Override
    public void addItem(String data) {
        items.add(data);
        notifyItemInserted(items.size() - 1);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        RowItemBinding binding;

        public ItemViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }

    public void flush() {
        items.clear();
        notifyDataSetChanged();
    }
}
