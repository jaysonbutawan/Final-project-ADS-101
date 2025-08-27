<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

// Database connection
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "sapa_coordinator_db";

try {
    $pdo = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    if ($_SERVER['REQUEST_METHOD'] == 'POST') {
        // Get POST data
        $student_id = $_POST['student_id'] ?? null;
        $user_id = $_POST['user_id'] ?? null;
        $firstname = $_POST['firstname'] ?? null;
        $lastname = $_POST['lastname'] ?? null;
        $phone_number = $_POST['phone_number'] ?? null;
        $email = $_POST['email'] ?? null;
        $sex = $_POST['sex'] ?? null;
        $date_of_birth = $_POST['date_of_birth'] ?? null;
        $school_id = $_POST['school_id'] ?? null;

        // Validate required fields
        if (empty($student_id) || empty($user_id) || empty($firstname) || empty($lastname) || empty($school_id)) {
            echo json_encode([
                'success' => false,
                'message' => 'Missing required fields'
            ]);
            exit;
        }

        // Calculate age from date of birth
        $age = 0;
        if (!empty($date_of_birth)) {
            $birth_date = new DateTime($date_of_birth);
            $current_date = new DateTime();
            $age = $current_date->diff($birth_date)->y;
        }

        // Check if student exists and belongs to the user's school
        $check_stmt = $pdo->prepare("
            SELECT s.student_id
            FROM Students s
            JOIN Schools sc ON s.school_id = sc.school_id
            WHERE s.student_id = ? AND sc.user_id = ? AND s.school_id = ?
        ");
        $check_stmt->execute([$student_id, $user_id, $school_id]);

        if ($check_stmt->rowCount() == 0) {
            echo json_encode([
                'success' => false,
                'message' => 'Student not found or access denied'
            ]);
            exit;
        }

        // Update student information
        $update_stmt = $pdo->prepare("
            UPDATE Students
            SET firstname = ?, lastname = ?, phone_number = ?, email = ?, sex = ?, age = ?
            WHERE student_id = ?
        ");

        $result = $update_stmt->execute([
            $firstname,
            $lastname,
            $phone_number,
            $email,
            $sex,
            $age,
            $student_id
        ]);

        if ($result) {
            echo json_encode([
                'success' => true,
                'message' => 'Student updated successfully'
            ]);
        } else {
            echo json_encode([
                'success' => false,
                'message' => 'Failed to update student'
            ]);
        }

    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Invalid request method'
        ]);
    }

} catch (PDOException $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Database error: ' . $e->getMessage()
    ]);
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
?>
