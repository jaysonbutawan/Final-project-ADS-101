package com.example.sapacoordinator.DatabaseConnector;

import com.example.sapacoordinator.ViewBookingComponents.ViewBooking.CancelRequest;
import com.example.sapacoordinator.ViewBookingComponents.ViewBooking.ViewBookingModel;
import com.example.sapacoordinator.HospitalComponents.DepartmentComponents.Department;
import com.example.sapacoordinator.HospitalComponents.Hospital;
import com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents.DateSlot;
import com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents.TimeSlotModel;
import com.example.sapacoordinator.SchoolComponents.School;
import com.example.sapacoordinator.SchoolComponents.StudentsComponents.Student;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    // Register user
    @FormUrlEncoded
    @POST("UserRegistration.php")
    Call<GenericResponse> registerUser(
            @Field("firstname") String firstname,
            @Field("lastname") String lastname,
            @Field("email") String email,
            @Field("password") String password
    );

    // Login user
    @FormUrlEncoded
    @POST("login.php")
    Call<GenericResponse> loginUser(
            @Field("email") String email,
            @Field("password") String password
    );

    // Add School
    @FormUrlEncoded
    @POST("register_school.php")
    Call<GenericResponse> registerSchool(
            @Field("school_name") String name,
            @Field("school_address") String address,
            @Field("contact_info") String contact,
            @Field("user_id") int user_id

    );

    @FormUrlEncoded
    @POST("get_schools.php")
    Call<List<School>> getSchools(
            @Field("user_id") int userId,
            @Field("status") String status
            );
    @FormUrlEncoded
    @POST("add_student.php")
    Call<GenericResponse> registerStudent(
            @Field("user_id") int userId,
            @Field("firstname") String firstName,
            @Field("lastname") String lastName,
            @Field("phone_number") String phoneNumber,
            @Field("email") String email,
            @Field("sex") String sex,
            @Field("date_of_birth") String dateOfBirth,
            @Field("school_id") int schoolId
    );

    @GET("count_students.php")
    Call<GenericResponse> getStudentCount(
            @Query("user_id") int userId,
            @Query("school_id") int schoolId
    );
    @GET("get_students.php")
    Call<List<Student>> getStudents(@Query("user_id") int userId, @Query("school_id") int schoolId);

    // Update student endpoint
    @FormUrlEncoded
    @POST("update_student.php")
    Call<GenericResponse> updateStudent(
            @Field("student_id") int studentId,
            @Field("user_id") int userId,
            @Field("firstname") String firstName,
            @Field("lastname") String lastName,
            @Field("phone_number") String phoneNumber,
            @Field("email") String email,
            @Field("sex") String sex,
            @Field("date_of_birth") String dateOfBirth,
            @Field("school_id") int schoolId
    );

    // Delete student endpoint
    @FormUrlEncoded
    @POST("delete_student.php")
    Call<GenericResponse> deleteStudent(
            @Field("student_id") int studentId,
            @Field("school_id") int schoolId
    );

    @GET("get_hospitals.php")
    Call<List<Hospital>> getHospitals();

    @GET("get_departments.php")
    Call<List<Department>> getDepartments(@Query("hospital_id") int hospitalId);

    // ApiInterface.java
    @GET("get_date_slots.php")
    Call<List<DateSlot>> getDateSlots(@Query("department_id") int departmentId);

    @GET("get_time_slots.php")
    Call<List<TimeSlotModel>> getTimeSlots(@Query("date_slot_id") int dateSlotId);

    // ✅ Booking submission endpoint


    @FormUrlEncoded
    @POST("book_appointment.php")
    Call<GenericResponse> bookAppointment(
            @Field("school_id") int schoolId,
            @Field("hospital_id") int hospitalId,
            @Field("department_id") int departmentId,
            @Field("slot_date_id") int dateSlotId,
            @Field("time_slot_id") int timeSlotId,
            @Field("student_ids") String studentIdsJson
    );
    @GET("get_available_students.php")
    Call<List<Student>> getAvailableStudents(
            @Query("user_id") int userId,
            @Query("school_id") int schoolId,
            @Query("time_slot_id") int timeSlotId
    );

    @GET("get_appointments.php")
    Call<List<ViewBookingModel>> getFilteredBookedStudents(
            @Query("school_id") int schoolId,
            @Query("hospital_id") int hospitalId,
            @Query("department_id") int departmentId,
            @Query("slot_date_id") int dateSlotId,
            @Query("time_slot_id") int timeSlotId
    );
    @GET("get_booking_counts.php")
    Call<GenericResponse> getBookingCount(
            @Query("school_id") int schoolId
    );

    // Cancel appointment endpoint
    @POST("cancel_appointment.php")
    Call<GenericResponse> cancelAppointment(@Body CancelRequest request);


}
