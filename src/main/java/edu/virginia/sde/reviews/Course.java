package edu.virginia.sde.reviews;

import java.util.Objects;

public class Course {
    private int id;
    private String mnemonic;
    private int number;
    private String title;

    public Course(int id, String mnemonic, int number, String title) {
        this.id = id;
        this.mnemonic = mnemonic.toUpperCase().trim();
        this.number = number;
        this.title = title.trim();
    }

    public Course(String mnemonic, int number, String title) {
        this.mnemonic = mnemonic.toUpperCase().trim();
        this.number = number;
        this.title = title.trim();
    }

    public Course() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic.toUpperCase().trim();
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title.trim();
    }

    @Override
    public String toString() {
        return "Course ID: " + id + ", Mnemonic: " + mnemonic + ", Number: " + number;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Course)) return false;
        Course course = (Course) o;
        return Objects.equals(mnemonic, course.mnemonic) &&
                Objects.equals(number, course.number) &&
                Objects.equals(title, course.title);
    }
}
