package ru.nordmine.model;

public class Translation {

	private String text;
	private String comment;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "Translation{" +
				"text='" + text + '\'' +
				", comment='" + comment + '\'' +
				'}';
	}
}
