import java.util.Scanner;
import java.sql.* ;

class VaccineApp
{
    public static void main ( String [ ] args ) throws SQLException
    {

        // Unique table names.  Either the user supplies a unique identifier as a command line argument, or the program makes one up.
        int sqlCode=0;      // Variable to hold SQLCODE
        String sqlState="00000";  // Variable to hold SQLSTATE

        // Register the driver.  You must register the driver before you can use it.
        try { DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ; }
        catch (Exception cnfe){ System.out.println("Class not found"); }

        // This is the url you must use for DB2.
        //Note: This url may not valid now !
        String url = "jdbc:db2://winter2021-comp421.cs.mcgill.ca:50000/cs421";

        //REMEMBER to remove your user id and password before submitting your code!!
        String your_userid = "";
        String your_password = "";

        //AS AN ALTERNATIVE, you can just set your password in the shell environment in the Unix (as shown below) and read it from there.
        //$  export SOCSPASSWD=yoursocspasswd
        if(your_userid == null && (your_userid = System.getenv("SOCSUSER")) == null)
        {
            System.err.println("Error!! do not have a password to connect to the database!");
            System.exit(1);
        }
        if(your_password == null && (your_password = System.getenv("SOCSPASSWD")) == null)
        {
            System.err.println("Error!! do not have a password to connect to the database!");
            System.exit(1);
        }



        Connection con = DriverManager.getConnection (url,your_userid,your_password) ;
        Statement stmt = con.createStatement ( ) ;


        while (true) {
            System.out.println("VaccineApp Main Menu\n" + "	1. Add a Person\n" +
                    "	2. Assign a slot to a Person\n" +
                    "	3. Enter Vaccination information\n" +
                    "	4. Exit Application\n"
                    + "Please Enter Your Option:");

            Scanner scan = new Scanner(System.in).useDelimiter("\n");
            int select = scan.nextInt();
            if (select == 1) {
                System.out.println("Please input user's insurance number: ");
                long here  = scan.nextLong();
                try
                {
                    Boolean found = false;
                    long ins = 0;
                    ResultSet res  = stmt.executeQuery("SELECT insNum from Person ");
                    while (res.next()) {
                        ins = res.getLong(1);
                        if (ins == here) {
                            found =  true;
                            break;
                        }
                    } //end of while loop

                    System.out.println("Please input user's category:");
                    String category = scan.next();
                    System.out.println("Please input user's complete name:");
                    String namey = scan.next();
                    System.out.println("Please input user's gender");
                    String gender = scan.next();
                    System.out.println("Please input user's date of birth:");
                    String dateOfBirth = scan.next();
                    System.out.println("Please input user's phone:");
                    long phone = scan.nextLong();
                    System.out.println("Please input user's city you live in:");
                    String city = scan.next();
                    System.out.println("Please input user's postal code:");
                    String postalCode = scan.next();
                    System.out.println("Please input user's street address");
                    String address = scan.next();
                    System.out.println("Please input today's date");
                    String date = scan.next();

                    if (found == false) {

                        PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO Person (insNum, cname, name , gender, dateOfBirth, phone, city, postalCd, streetAddr, regDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                        preparedStatement.setLong(1, here);
                        preparedStatement.setString(2, category);
                        preparedStatement.setString(3, namey);
                        preparedStatement.setString(4, gender);
                        preparedStatement.setDate(5, java.sql.Date.valueOf(dateOfBirth));
                        preparedStatement.setLong(6, phone);
                        preparedStatement.setString(7, city);
                        preparedStatement.setString(8, postalCode);
                        preparedStatement.setString(9, address);
                        preparedStatement.setDate(10, java.sql.Date.valueOf(date));

                        preparedStatement.executeUpdate();

                        System.out.println("User has been successfully registered!\n");

                    } else {

                        System.out.println("This insurance number is already registered \nWould you like to update current user information?");
                        System.out.println("1.- Yes");
                        System.out.println("2.- No");

                        int answer = scan.nextInt();

                        if (answer == 1){

                            PreparedStatement prep = con.prepareStatement("UPDATE Person SET (cname, name , gender, dateOfBirth, phone, city, postalCd, streetAddr, regDate) =(?, ?, ?, ?, ?, ?, ?, ?, ?) WHERE insNum = ?");

                            prep.setString(1, category);
                            prep.setString(2, namey);
                            prep.setString(3, gender);
                            prep.setDate(4, java.sql.Date.valueOf(dateOfBirth));
                            prep.setLong(5, phone);
                            prep.setString(6, city);
                            prep.setString(7, postalCode);
                            prep.setString(8, address);
                            prep.setDate(9, java.sql.Date.valueOf(date));
                            prep.setLong(10, here);

                            prep.executeUpdate();

                            System.out.println("User information has been successfully updated!\n");

                        }
                        else {
                            System.out.println("Thank you\n");
                            continue;
                        }
                    }
                }
                catch (SQLException e)
                {
                    sqlCode = e.getErrorCode(); // Get SQLCODE
                    sqlState = e.getSQLState(); // Get SQLSTATE

                    // Your code to handle errors comes here;
                    // something more meaningful than a print would be good
                    System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
                    System.out.println(e);
                }

            }
            else if (select == 2) {

                System.out.println("Please input insurance number: ");
                long here  = scan.nextLong();

                System.out.println("Please input today's date: ");
                String today = scan.next();

                System.out.println("Please input a slot location: ");
                String loc = scan.next();

                System.out.println("Please input a date for the slot : ");
                String sDate = scan.next();

                System.out.println("Please input a slot number: ");
                int slot = scan.nextInt();

                System.out.println("Please input a time for the slot: ");
                String time = scan.next();

                Date slotDate = java.sql.Date.valueOf(sDate);
                Date toDate = java.sql.Date.valueOf(today);

                if (slotDate.before(toDate)) {

                    System.out.println("Please input a date after today: ");
                    continue;
                }

                //INSERT NEW ROW IN TABLE "ASSIGNED" IF USER HAS NOT RECEIVED A VACCINE YET OR IS MISSING DOSES
                //ELSE DISPLAY ERROR MESSAGE

               try {
                    int counter = 0;
                    PreparedStatement prep  = con.prepareStatement("SELECT insNum, vName FROM Assigned JOIN Vial ON (Assigned.locName, Assigned.vDate, Assigned.vSlot, Assigned.vTime) = (Vial.locName, Vial.vDate, Vial.vSlot, Vial.vTime) WHERE insNum = ?");
                    prep.setLong(1, here);
                    ResultSet res  = prep.executeQuery();
                    String vac = "";
                    while (res.next()) {
                       counter++;
                       vac = res.getString("vName");
                    } //end of while loop

                    if (counter == 0) {

                        Boolean found = false;

                        System.out.println("User's first vaccine!");

                        PreparedStatement pAvailable  = con.prepareStatement("(SELECT VacSlot.locName, VacSlot.vDate, VacSlot.vSlot, VacSlot.vTime FROM VacSlot WHERE VacSlot.vDate >= ?) EXCEPT (SELECT Assigned.locName, Assigned.vDate, Assigned.vSlot, Assigned.vTime FROM ASSIGNED JOIN VacSlot ON (Assigned.locName, Assigned.vDate, Assigned.vSlot, Assigned.vTime) = (VacSlot.locName, VacSlot.vDate, VacSlot.vSlot, VacSlot.vTime))");

                        pAvailable.setDate(1, toDate);

                        ResultSet available = pAvailable.executeQuery();

                        while (available.next()) {

                            if ((available.getString("locName").equals(loc)) && (available.getString("vDate").equals(sDate) ) && (available.getInt("vSlot")== slot) &&(available.getString("vTime").equals(time))){

                                found = true;

                                PreparedStatement pAdd  = con.prepareStatement("INSERT INTO Assigned (insNum, asgDate, locName, vDate, vSlot, vTime)  VALUES (?,?,?,?,?,?)");

                                pAdd.setLong(1, here);
                                pAdd.setDate(2, toDate);
                                pAdd.setString(3, loc);
                                pAdd.setDate(4, slotDate);
                                pAdd.setInt(5, slot);
                                pAdd.setTime(6, java.sql.Time.valueOf(time));

                                pAdd.executeUpdate();

                                break;

                            }


                        }

                        if (found){
                            System.out.println("Succesfully assigned a vaccination slot to user \n");
                        }
                        else {
                            System.out.println("No available vaccination slots\n");
                        }

                    }  //END OF IF COUNTER == 0
                    else {

                        int doses = 0;

                        PreparedStatement down = con.prepareStatement("SELECT doses FROM Vaccine WHERE vName = ?");

                        down.setString(1, vac);

                        ResultSet m  = down.executeQuery();

                        if (m.next()) {
                            doses = m.getInt(1);
                        }
                        if (counter >= doses) {

                            System.out.println("User has received all doses\n");

                        } else if (counter < doses) {

                            System.out.println("User has "+ (doses-counter)+ " doses remaining");

                            Boolean found = false;


                            PreparedStatement pAvailable  = con.prepareStatement("(SELECT VacSlot.locName, VacSlot.vDate, VacSlot.vSlot, VacSlot.vTime FROM VacSlot WHERE VacSlot.vDate >= ?) EXCEPT (SELECT Assigned.locName, Assigned.vDate, Assigned.vSlot, Assigned.vTime FROM ASSIGNED JOIN VacSlot ON (Assigned.locName, Assigned.vDate, Assigned.vSlot, Assigned.vTime) = (VacSlot.locName, VacSlot.vDate, VacSlot.vSlot, VacSlot.vTime))");

                            pAvailable.setDate(1, toDate);

                            ResultSet available = pAvailable.executeQuery();

                            while (available.next()) {

                                if ((available.getString("locName").equals(loc)) && (available.getString("vDate").equals(sDate) ) && (available.getInt("vSlot")== slot) &&(available.getString("vTime").equals(time))){

                                    found = true;

                                    PreparedStatement pAdd  = con.prepareStatement("INSERT INTO Assigned (insNum, asgDate, locName, vDate, vSlot, vTime)  VALUES (?,?,?,?,?,?)");

                                    pAdd.setLong(1, here);
                                    pAdd.setDate(2, toDate);
                                    pAdd.setString(3, loc);
                                    pAdd.setDate(4, slotDate);
                                    pAdd.setInt(5, slot);
                                    pAdd.setTime(6, java.sql.Time.valueOf(time));

                                    pAdd.executeUpdate();

                                    break;

                                }


                            }

                            if (found){
                                System.out.println("Succesfully assigned a vaccination slot to user \n");
                            }
                            else {
                                System.out.println("No available vaccination slots\n");
                            }

                            }

                        }

                }
                catch (SQLException e)
                {
                    sqlCode = e.getErrorCode(); // Get SQLCODE
                    sqlState = e.getSQLState(); // Get SQLSTATE

                    // Your code to handle errors comes here;
                    // something more meaningful than a print would be good
                    System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
                    System.out.println(e);
                }

            }
            else if (select == 3) {

                System.out.println("Please input insurance number: ");
                long num  = scan.nextLong();

                System.out.println("Please input vaccine brand: ");
                String vNam  = scan.next();

                System.out.println("Please input vaccine's batch number: ");
                int bNum  = scan.nextInt();

                System.out.println("Please input vial number: ");
                int vialNum  = scan.nextInt();

                System.out.println("Please input nurse license number: ");
                long nurseLicense = scan.nextLong();

                PreparedStatement aPrevious = con.prepareStatement("SELECT vName FROM Vial JOIN Assigned ON (Assigned.locName, Assigned.vDate, Assigned.vSlot, Assigned.vTime) = (Vial.locName, Vial.vDate, Vial.vSlot, Vial.vTime) WHERE insNum = ?");

                aPrevious.setLong(1, num);

                ResultSet previous = aPrevious.executeQuery();

                if (previous.next()){

                    String fName = previous.getString(1);

                    if (fName !=vNam){

                        System.out.println("You can't administer a different vaccine brand to a previously vaccinated user");
                        continue;
                    }

                }

                PreparedStatement pSlot  = con.prepareStatement("(SELECT Assigned.locName, Assigned.vDate, Assigned.vSlot, Assigned.vTime FROM Assigned WHERE Assigned.insNum = ?) EXCEPT (SELECT Vial.locName, Vial.vDate, Vial.vSlot, Assigned.vTime FROM Vial JOIN Assigned ON (Assigned.locName, Assigned.vDate, Assigned.vSlot, Assigned.vTime) = (Vial.locName, Vial.vDate, Vial.vSlot, Vial.vTime) WHERE insNum = ?)");

                pSlot.setLong(1,num);
                pSlot.setLong(2,num);

                ResultSet space = pSlot.executeQuery();

                if (space.next()){
                    //Get nurse's license number for this slot at the given date (AssignedTo table)
                    //Use this to update (Administered table)
                    //If everything is correct update (Vial table)

                String loc = space.getString(1);
                String date = space.getString(2);
                int slot = space.getInt(3);
                String time = space.getString(4);

                PreparedStatement pNurse  = con.prepareStatement("SELECT licNum FROM AssignedTo WHERE licNum = ? AND (vDate,locName ) =  (?, ?)");

                pNurse.setLong(1,nurseLicense);
                pNurse.setDate(2,java.sql.Date.valueOf(date));
                pNurse.setString(3,loc);

                ResultSet nurse = pNurse.executeQuery();

                if (nurse.next()) {

                    long license = nurse.getLong(1);

                    PreparedStatement addVial  = con.prepareStatement("INSERT INTO Vial (vName, batchNo, vialId, locName, vDate, vSlot, vTime) VALUES (?,?,?,?,?,?,?)");

                    addVial.setString(1,vNam);
                    addVial.setInt(2,bNum);
                    addVial.setInt(3,vialNum);
                    addVial.setString(4,loc);
                    addVial.setDate(5,java.sql.Date.valueOf(date));
                    addVial.setInt(6,slot);
                    addVial.setTime(7,java.sql.Time.valueOf(time));

                    addVial.executeUpdate();

                    PreparedStatement addNurse  = con.prepareStatement("INSERT INTO Admins (locName, vDate, vSlot, vTime, licenseNum) VALUES (?,?,?,?,?)");

                    addNurse.setString(1,loc);
                    addNurse.setDate(2,java.sql.Date.valueOf(date));
                    addNurse.setInt(3,slot);
                    addNurse.setTime(4,java.sql.Time.valueOf(time));
                    addNurse.setLong(5,license);

                    addNurse.executeUpdate();

                    System.out.println("Successfully added vaccination information");

                } else {

                    System.out.println("Selected nurse is not associated for this location and date");
                }

                } else {
                    System.out.println("User does not have a previously assigned vaccination slot, it can't be vaccinated");

                }


            }

            else if (select == 4) {
                stmt.close ( ) ;
                con.close ( ) ;
                scan.close();
                System.out.println("Thank you for using Vaccine App!");
                break;

            } else {

                System.out.println("Please input one option between 1 and 4");

            }
        }

    }
}

