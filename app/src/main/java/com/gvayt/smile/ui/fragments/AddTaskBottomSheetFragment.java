package com.gvayt.smile.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.gvayt.smile.R;

public class AddTaskBottomSheetFragment extends BottomSheetDialogFragment {

    private EditText etTaskName;
    private EditText etTaskTime;
    private MaterialButton btnSave, btnCancel;

    private OnTaskAddedListener listener;

    public interface OnTaskAddedListener {
        void onTaskAdded(String name, String time);
    }

    public void setOnTaskAddedListener(OnTaskAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_task_bottom_sheet, container, false);

        etTaskName = view.findViewById(R.id.et_task_name);
        etTaskTime = view.findViewById(R.id.et_task_time);
        btnSave = view.findViewById(R.id.btn_save_task);
        btnCancel = view.findViewById(R.id.btn_cancel_task);

        btnSave.setOnClickListener(v -> {
            String name = etTaskName.getText().toString().trim();
            String time = etTaskTime.getText().toString().trim();

            if (name.isEmpty() || time.isEmpty()) {
                etTaskName.setError("Введите название");
                etTaskTime.setError("Введите время");
                return;
            }

            if (listener != null) {
                listener.onTaskAdded(name, time);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }
}
