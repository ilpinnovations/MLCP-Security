<?php
/**
 * Created by PhpStorm.
 * User: Milind
 * Date: 7/10/15
 * Time: 10:46 PM
 */

namespace MLCP;
include 'MLCPAccess.php';

///////////////////////// MAIN CODE ///////////////////////////////////
if (VRM::Validate()) {

    $mlcp = new MLCPAccess();
    $response_string = "";

    switch (VRM::$TAG) {

        case MLCPAccess::tagGetFloors:
            $response_string = $mlcp->getFloors();
            break;

        case MLCPAccess::tagGetBookingDetails:
            $response_string = $mlcp->getBookingDetails(VRM::$BOOKING_ID);
            break;

        case MLCPAccess::tagConfirmParking:
            $response_string = $mlcp->confirmParking(VRM::$BOOKING_ID);
            break;

	case MLCPAccess::tagSwipeOut:
            $response_string = $mlcp->swipeOut(VRM::$BOOKING_ID);
            break;

        case MLCPAccess::tagClearSlot:
            $response_string = $mlcp->clearSlot(VRM::$SLOT_ID);
            break;

	case MLCPAccess::tagReserveSlot:
            $response_string = $mlcp->reserveSlot(VRM::$SLOT_ID);
            break;

        case MLCPAccess::tagChangeSlot:
            $response_string = $mlcp->changeSlot(VRM::$BOOKING_ID, VRM::$VEHICLE_NUMBER);
            break;

        case MLCPAccess::tagGetAllSlots:
            $response_string = $mlcp->getAllSlots();
            break;

        case MLCPAccess::tagGetReservedSlots:
            $response_string = $mlcp->getReservedSlots();
            break;

        case MLCPAccess::tagGetConfirmedSlots:
            $response_string = $mlcp->getConfirmedSlots();
            break;

        case MLCPAccess::tagBookSlot:
            $response_string = $mlcp->bookSlot(VRM::$SLOT_ID, VRM::$VEHICLE_NUMBER, VRM::$EMPLOYEE_ID);
            break;

        default:
            $response_string = "Invalid parameter(s) passed. Aborting!!";
            break;
    }

    echo $response_string;

} else {
    echo "Invalid Request Found. Aborting!!";
    exit;
}

