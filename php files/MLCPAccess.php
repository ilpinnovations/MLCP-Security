<?php
/**
 * Created by PhpStorm.
 * User: Milind
 * Date: 7/10/15
 * Time: 3:05 PM
 */

namespace MLCP;
require 'dbcon.php';

class MLCPAccess {

    const tagGetFloors = "GetFloors";
    const tagGetBookingDetails = "GetBookingDetails";
    const tagConfirmParking = "ConfirmParking";
    const tagClearSlot = "ClearSlot";
    const tagChangeSlot = "ChangeSlot";
    const tagGetAllSlots = "GetAllSlots";
    const tagGetReservedSlots = "GetReservedSlots";
    const tagGetConfirmedSlots = "GetConfirmedSlots";
    const tagBookSlot = "BookSlot";
    const tagReserveSlot = "ReserveSlot";
    const tagSwipeOut = "SwipeOut";

    private $host = "localhost";
    private $dbname = "mlcp";
    private $username = "mlcp";
    private $password = "helloworld";

    private $BASE_URL = "localhost";
    private $connection = NULL;
    private $isInitialized = false;
    private $tag = "";
    private $error = false;
    private $errorMsg = "";
    private $result = null;

    function __construct() {
        $this->isInitialized = true;
    }

    public function getFloors() {
        $this->openDBConnection();
        $this->tag = MLCPAccess::tagGetFloors;
        $this->result = $this->executeQuery("SELECT floorid, floorname FROM mlcp_floor WHERE 1 ORDER BY floorid");
        $this->closeDBConnection();
        return $this->prepareResponse();
    }
    public function getBookingDetails($bookingId) {
        $this->openDBConnection();
        $this->tag = MLCPAccess::tagGetBookingDetails;
        $this->result = $this->executeQuery("SELECT booking_id, slotid, BOOK.employeeid, vehiclenumber FROM mlcp_booking BOOK, mlcp_vehicle VEH WHERE BOOK.employeeid = VEH.employeeid AND booking_id > $bookingId AND BOOK.isconfirmed=0 order by BOOK.booking_id");
        $this->closeDBConnection();
        return $this->prepareResponse();
    }
    public function getAllSlots() {
        $this->openDBConnection();
        $this->tag = MLCPAccess::tagGetAllSlots;
        $this->result = $this->executeQuery("SELECT slotid,slotname,status,isreserved,vehiclesize FROM mlcp_slot WHERE isreserved=0 ORDER BY slotid");
        $this->closeDBConnection();
        return $this->prepareResponse();
    }
    public function getReservedSlots() {
        $this->openDBConnection();
        $this->tag = MLCPAccess::tagGetReservedSlots;
        $this->result = $this->executeQuery("select slotid,slotname from mlcp_slot WHERE isreserved = 1 ORDER BY slotid");
        $this->closeDBConnection();
        return $this->prepareResponse();
    }
    public function getConfirmedSlots() {
        $this->openDBConnection();
        $this->tag = MLCPAccess::tagGetConfirmedSlots;
        $this->result = $this->executeQuery("SELECT BOOK.booking_id, BOOK.slotid, BOOK.employeeid, vehiclenumber, status FROM mlcp_booking BOOK, mlcp_vehicle VEH, mlcp_slot SLOT WHERE BOOK.employeeid = VEH.employeeid AND BOOK.slotid = SLOT.slotid AND isconfirmed = 1 AND BOOK.book_timeout IS NULL order by booking_id");
        $this->closeDBConnection();
        return $this->prepareResponse();
    }
    public function confirmParking($bookingId) {
        $this->openDBConnection(); 
        $this->tag = MLCPAccess::tagConfirmParking;
        $this->result = $this->executeQuery("UPDATE mlcp_booking SET isconfirmed = 1 WHERE booking_id = $bookingId");
        $this->error = $this->result === false;
        $this->result = false;

        $this->closeDBConnection();
        return $this->prepareResponse();
    }
	public function swipeOut($bookingId) {
        $this->openDBConnection(); 
        $this->tag = MLCPAccess::tagSwipeOut;
        $this->result = $this->executeQuery("SELECT slotid FROM mlcp_booking WHERE booking_id = $bookingId");
        $result = mysql_fetch_array($this->result);
        $this->result = $this->executeQuery("UPDATE mlcp_booking SET status=0 WHERE slotid = $result[0]");
        $this->result = $this->executeQuery("UPDATE mlcp_booking SET book_timeout = NOW() WHERE booking_id = $bookingId");
        $this->error = $this->result === false;
        $this->result = false;
        $this->closeDBConnection();
        return $this->prepareResponse();
    }

    public function clearSlot($slotId) {
        $this->openDBConnection();
        $this->tag = MLCPAccess::tagClearSlot;
        $this->result = $this->executeQuery("UPDATE mlcp_slot SET status = 0, isreserved = 0 WHERE slotid = $slotId");
        $this->error = $this->result === false;
        $this->result = false;
        $this->closeDBConnection();
        return $this->prepareResponse();

    }
    public function reserveSlot($slotId) {
        $this->openDBConnection();
        $this->tag = MLCPAccess::tagReserveSlot;
        $this->result = $this->executeQuery("UPDATE mlcp_slot SET status = 0, isreserved = 1 WHERE slotid = $slotId");
        $this->error = $this->result === false;
        $this->result = false;
        $this->closeDBConnection();
        return $this->prepareResponse();

    }
    public function changeSlot($bookingId, $newVehicleNumber) {
        $this->openDBConnection();
        $this->tag = MLCPAccess::tagChangeSlot;

        $q = "SELECT employeeid FROM mlcp_vehicle WHERE vehiclenumber = '".$newVehicleNumber."'";
        $this->result = $this->executeQuery($q);
        if ($this->result === false) {
            $tRow = null;
        } else {
            $tRow = mysqli_fetch_assoc($this->result);
        }
        if (!$tRow) {
            $employeeId = null;
            $this->result = false;
        } else {
            $employeeId = $tRow['employeeid'];
        }

        if ($employeeId) {
            $q = "UPDATE MLCP_BOOKING SET EMPLOYEEID = '".$employeeId."' WHERE BOOKING_ID = '".$bookingId."'";
            $this->result = $this->executeQuery($q);
        }

        $this->error = $this->result === false;
        $this->result = false;

        $this->closeDBConnection();
        return $this->prepareResponse();
    }
    public function bookSlot($slotId, $vehicleNumber, $employeeId) {
        $this->clearSlot($slotId);
        $this->openDBConnection();
        $this->tag = MLCPAccess::tagBookSlot;

        if ($vehicleNumber) {

            $q = "SELECT employeeid FROM mlcp_vehicle WHERE vehiclenumber = '".$vehicleNumber."'";
            $this->result = $this->executeQuery($q);
            if ($this->result === false) {
                $tRow = null;
            } else {
                $tRow = mysqli_fetch_assoc($this->result);
            }
            if (!$tRow) {
                $employeeId = null;
                $this->result = false;
            } else {
                $employeeId = $tRow['employeeid'];
            }
        }
        if ($employeeId)
            $this->result = $this->executeQuery("INSERT INTO mlcp_booking (slotid, employeeid, book_timeIn, book_timeout, isconfirmed) VALUES ($slotId, $employeeId, NOW(), NULL, 1)");

        $this->error = $this->result === false;
        $this->result = false;

        $this->closeDBConnection();
        return $this->prepareResponse();
    }


    /// open connection to the database
    private function openDBConnection() {
        if ($this->connection)
            $this->closeDBConnection();

        $this->connection = mysqli_connect($this->host, $this->username, $this->password, $this->dbname);
        if (!$this->connection) {
            throw new Exception("Unable to connect to database");
        }
    }

    /// execute the query and store the result in the variable
    private function executeQuery($query) {
        return mysqli_query($this->connection, $query);
    }

    /// prepare the response to send
    private function prepareResponse() {

        $values = array();

        if ($this->result) {
            while ($row = mysqli_fetch_assoc($this->result)) {
                array_push($values, $row);
            }
            $this->error = sizeof($values) == 0;
        }

        if ($this->error) {
        
            switch($this->tag)
            {
                case MLCPAccess::tagGetFloors:
                    $this->errorMsg = "Could not get the floors or floors empty."; break;
                case MLCPAccess::tagGetBookingDetails:
                    $this->errorMsg = "No booking details loaded."; break;
                case MLCPAccess::tagConfirmParking:
                    $this->errorMsg = "Could not confirm parking."; break;
                case MLCPAccess::tagClearSlot:
                    $this->errorMsg = "Could not clear the slot."; break;
                case MLCPAccess::tagReserveSlot:
                    $this->errorMsg = "Could not reserve the slot."; break;
                case MLCPAccess::tagSwipeOut:
                    $this->errorMsg = "Could not swipe out."; break;
                case MLCPAccess::tagChangeSlot:
                    $this->errorMsg = "Could not change the slot."; break;
                case MLCPAccess::tagGetAllSlots:
                    $this->errorMsg = "No slots loaded."; break;
                case MLCPAccess::tagGetReservedSlots:
                    $this->errorMsg = "No reserved slots loaded."; break;
                case MLCPAccess::tagGetConfirmedSlots:
                    $this->errorMsg = "No confirmed slots loaded."; break;
                case MLCPAccess::tagBookSlot:
                    $this->errorMsg = "Could not book the slot."; break;            }
        }
        $response = array (
            "tag" => $this->tag,
            "error" => $this->error
        );
        if ($this->error) {
            $response["errorMsg"] = $this->errorMsg;
        } else {
            $response["values"] = $values;
        }

        return json_encode($response);
    }
    /// close the connection to the database
    private function closeDBConnection() {
        if ($this->connection) {
            mysqli_close($this->connection);
            $this->connection = null;
        }
    }
}




function CHK ($x) {
    return isset($_REQUEST[$x]);
}
function GET($x) {
    if (CHK($x))
        return $_REQUEST[$x];
    else
        return null;
}


/// VALIDATION REQUEST MANAGER
class VRM {

    public static $TAG;
    public static $BOOKING_ID;
    public static $SLOT_ID;
    public static $VEHICLE_NUMBER;
    public static $EMPLOYEE_ID;

    public static function Validate() {
        $isValid = true;
        if (!isset($_REQUEST['tag']))
        {
            $isValid = false;
            return $isValid;
        }

        VRM::$TAG = $_REQUEST['tag'];
        switch (VRM::$TAG) {

            case MLCPAccess::tagGetFloors:
                $isValid = true;
                break;

            case MLCPAccess::tagGetBookingDetails:
                $isValid = CHK("bookingId");
                VRM::$BOOKING_ID = GET("bookingId");
                break;

            case MLCPAccess::tagConfirmParking:
                $isValid = CHK("bookingId");
                VRM::$BOOKING_ID = GET("bookingId");
                break;
                
            case MLCPAccess::tagSwipeOut:
                $isValid = CHK("bookingId");
                VRM::$BOOKING_ID = GET("bookingId");
                break;

            case MLCPAccess::tagClearSlot:
                $isValid = CHK("slotId");
                VRM::$SLOT_ID = GET("slotId");
                break;
                
            case MLCPAccess::tagReserveSlot:
                $isValid = CHK("slotId");
                VRM::$SLOT_ID = GET("slotId");
                break;

            case MLCPAccess::tagChangeSlot:
                $isValid = CHK("bookingId") AND CHK("newVehicleNumber");
                VRM::$BOOKING_ID = GET("bookingId");
                VRM::$VEHICLE_NUMBER = GET("newVehicleNumber");
                break;

            case MLCPAccess::tagGetAllSlots:
                $isValid = true;
                break;

            case MLCPAccess::tagGetReservedSlots:
                $isValid = true;
                break;

            case MLCPAccess::tagGetConfirmedSlots:
                $isValid = true;
                break;

            case MLCPAccess::tagBookSlot:
                $isValid = CHK("slotId") AND (CHK("vehicleNumber") OR CHK("employeeId"));
                VRM::$SLOT_ID = GET("slotId");
                VRM::$VEHICLE_NUMBER = GET("vehicleNumber");
                VRM::$EMPLOYEE_ID = GET("employeeId");
                break;
        }

        return $isValid;
    }

}
