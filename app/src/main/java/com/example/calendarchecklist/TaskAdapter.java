package com.example.calendarchecklist;

import android.annotation.SuppressLint;
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
import com.example.calendarchecklist.TaskContract;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private Cursor mCursor;
    private MainActivity mActivity;

    /**
     * 构造方法
     * @param activity 主Activity实例，用于回调
     * @param cursor   包含任务数据的游标
     */
    public TaskAdapter(MainActivity activity, Cursor cursor) {
        this.mActivity = activity;
        this.mCursor = cursor;
    }

    /**
     * 构造方法
     * @param activity 主Activity实例，用于回调
     * @param cursor   包含任务数据的游标
     */
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    /**
     * 将数据绑定到指定位置的视图项
     * @param holder   视图持有者
     * @param position 在列表中的位置
     */
    @SuppressLint("Range")
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        if (mCursor == null || !mCursor.moveToPosition(position)) return;// 游标为空或移动到指定位置失败则返回
        // 获取游标中各列的索引（此处直接使用列名获取，建议在正式代码中先获取索引并缓存）
        @SuppressLint("Range") long logId = mCursor.getLong(mCursor.getColumnIndex(TaskContract.TaskLogEntry._ID));
        long taskDefId = mCursor.getLong(mCursor.getColumnIndex(TaskContract.TaskLogEntry.COLUMN_TASK_ID)); // 需要从查询中获取
        String taskName = mCursor.getString(mCursor.getColumnIndex("task_name"));
        int completed = mCursor.getInt(mCursor.getColumnIndex(TaskContract.TaskLogEntry.COLUMN_COMPLETED));
        int isSpecial = mCursor.getInt(mCursor.getColumnIndex(TaskContract.TaskDefinitionEntry.COLUMN_IS_SPECIAL));
        int isDaily = mCursor.getInt(mCursor.getColumnIndex(TaskContract.TaskDefinitionEntry.COLUMN_IS_DAILY));
        // 设置任务名称
        holder.textViewTask.setText(taskName);
        // 根据是否为特殊事件决定是否显示复选框
        if (isSpecial == 1) {
            holder.checkBoxCompleted.setVisibility(View.INVISIBLE);
        } else {
            holder.checkBoxCompleted.setVisibility(View.VISIBLE);
            holder.checkBoxCompleted.setTag(logId);
            holder.checkBoxCompleted.setChecked(completed == 1);
            // 监听复选框状态变化
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

    /**
     * 获取当前数据列表项数量
     * @return 游标中记录的数量；若游标为空则返回0
     */
    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    /**
     * 替换适配器的游标数据
     * 调用此方法时，会关闭旧游标并更新为新游标，同时刷新列表
     * @param newCursor 新的游标对象（可能为null）
     */
    @SuppressLint("NotifyDataSetChanged")
    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }

    /**
     * 任务列表项的视图持有者类
     * 缓存 item_task 布局中的各个子视图，避免重复查找
     */
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxCompleted;
        TextView textViewTask;
        ImageButton buttonDelete;
        /**
         * 构造方法
         * @param itemView 列表项根视图
         */
        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxCompleted = itemView.findViewById(R.id.checkBoxCompleted);
            textViewTask = itemView.findViewById(R.id.textViewTask);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}