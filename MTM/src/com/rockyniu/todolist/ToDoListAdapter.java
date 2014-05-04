package com.rockyniu.todolist;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rockyniu.todolist.database.ToDoItem;

public class ToDoListAdapter extends ArrayAdapter<ToDoItem> {

	private final List<ToDoItem> list;
	private final Context context;
	private static LayoutInflater inflater = null;

	public ToDoListAdapter(Activity context, List<ToDoItem> list) {
		super(context, R.layout.row_layout, list);
		this.context = context;
		this.list = list;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public ToDoItem getItem(int position) {
		return list.get(position);
	}

	public void updateList(List<ToDoItem> newlist) {
		list.clear();
		list.addAll(newlist);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.row_layout, null);
		}

		ImageView imagePriority = (ImageView) view
				.findViewById(R.id.priority_icon);
		TextView title = (TextView) view.findViewById(R.id.todo_title);
		ImageView imageWarning = (ImageView) view
				.findViewById(R.id.warning_icon);
		TextView notesView = (TextView) view.findViewById(R.id.todo_notes);

		ToDoItem currentItem = list.get(position);

		TypedArray array = parent
				.getContext()
				.getTheme()
				.obtainStyledAttributes(
						new int[] { android.R.attr.textColorPrimary,
								android.R.attr.textColorSecondary });
		int colorTextPrimary = array.getColor(0, 0xFF00FF);
		int colorTextSecondary = array.getColor(1, 0xFF00FF);
//		array.recycle();

		if (currentItem.isCompleted()) {
			title.setPaintFlags(title.getPaintFlags()
					| Paint.STRIKE_THRU_TEXT_FLAG);
			title.setTextColor(Color.GREEN);
			
			Calendar completedTime = Calendar.getInstance();
			if (currentItem.getCompletedTime() == null){
				
			} else{
				completedTime.setTimeInMillis(currentItem.getCompletedTime());
				SimpleDateFormat format = new SimpleDateFormat(
						"MM/dd/yyyy @ HH:mm a", Locale.getDefault());
				notesView.setText("Completed on "
						+ format.format(completedTime.getTime()));
				notesView.setVisibility(View.VISIBLE);
				notesView.setTextColor(Color.GREEN);
			}
		} else {
			// Take off strike-through for a pending task
			title.setPaintFlags(title.getPaintFlags()
					& (~Paint.STRIKE_THRU_TEXT_FLAG));

			// Set the date due for the subtitle
			Long due = currentItem.getDueTime();
			String note = currentItem.getNotes();
			String notes = null;
			if (due == null) {
				if (note == null || note.isEmpty()) {
					notesView.setVisibility(View.GONE);
				} else {
					notesView.setVisibility(View.VISIBLE);
					notes = note;
				}
			} else {
				notesView.setVisibility(View.VISIBLE);
				Calendar dueTime = Calendar.getInstance();
				dueTime.setTimeInMillis(due);
				SimpleDateFormat format = new SimpleDateFormat(
						"MM/dd/yyyy @ HH:mm a", Locale.getDefault());
				notes = "Due on " + format.format(dueTime.getTime());
				if (note == null || note.isEmpty()) {

				} else {
					notes = note + "\n" + notes;
				}
			}
			if (notes!=null){
				notesView.setText(notes);
			}
			
		}

		// set priority icon
		if (currentItem.getPriority() == 0) {
			imagePriority.setImageResource(R.drawable.low);
		} else if (currentItem.getPriority() == 1) {
			imagePriority.setImageResource(R.drawable.median);
		} else {
			imagePriority.setImageResource(R.drawable.high);
		}

		// check if item past due.
		if (currentItem.isPastDue()) {
			title.setTextColor(Color.RED);
			notesView.setTextColor(Color.RED);
			imageWarning.setVisibility(View.VISIBLE);
		} else if (currentItem.isCompleted()) {
			title.setTextColor(Color.GREEN);
			notesView.setTextColor(Color.GREEN);
			imageWarning.setVisibility(View.GONE);
		} else {
			title.setTextColor(colorTextPrimary);
			notesView.setTextColor(colorTextSecondary);
			imageWarning.setVisibility(View.GONE);
		}

		String itemName = currentItem.getTitle();
		title.setText(itemName);

		return view;
	}
}
