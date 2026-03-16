package com.example.calendarchecklist;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Cursor mCursor;
    private MainActivity mActivity;

    public TaskAdapter(MainActivity activity, Cursor cursor) {
        this.mActivity = activity;
        this.mCursor = cursor;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        if (mCursor == null || !mCursor.moveToPosition(position)) return;

        long logId = mCursor.getLong(mCursor.getColumnIndex(TaskContract.TaskLogEntry._ID));
        long taskDefId = mCursor.getLong(mCursor.getColumnIndex(TaskContract.TaskLogEntry.COLUMN_TASK_ID)); // 需要从查询中获取
        String taskName = mCursor.getString(mCursor.getColumnIndex("task_name"));
        int completed = mCursor.getInt(mCursor.getColumnIndex(TaskContract.TaskLogEntry.COLUMN_COMPLETED));
        int isSpecial = mCursor.getInt(mCursor.getColumnIndex(TaskContract.TaskDefinitionEntry.COLUMN_IS_SPECIAL));
        int isDaily = mCursor.getInt(mCursor.getColumnIndex(TaskContract.TaskDefinitionEntry.COLUMN_IS_DAILY));

        holder.textViewTask.setText(taskName);

        if (isSpecial == 1) {
            holder.checkBoxCompleted.setVisibility(View.GONE);
        } else {
            holder.checkBoxCompleted.setVisibility(View.VISIBLE);
            holder.checkBoxCompleted.setTag(logId);
            holder.checkBoxCompleted.setChecked(completed == 1);
            holder.checkBoxCompleted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    long id = (long) buttonView.getTag();
                    mActivity.updateTaskStatus(id, isChecked);
                }
            });
        }

        // 设置删除按钮点击事件
        holder.buttonDelete.setTag(new long[]{taskDefId, isDaily}); // 用数组传递多个值
        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long[] tag = (long[]) v.getTag();
                long defId = tag[0];
                boolean isDailyTask = tag[1] == 1;
                mActivity.showDeleteDialog(defId, isDailyTask);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxCompleted;
        TextView textViewTask;
        ImageButton buttonDelete;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxCompleted = itemView.findViewById(R.id.checkBoxCompleted);
            textViewTask = itemView.findViewById(R.id.textViewTask);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}