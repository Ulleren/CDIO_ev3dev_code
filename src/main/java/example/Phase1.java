package example;

import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import ev3dev.actuators.lego.motors.EV3MediumRegulatedMotor;
import ev3dev.sensors.Battery;
import ev3dev.sensors.ev3.EV3GyroSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

import java.io.IOException;

public class Phase1 {
    static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S1);
    static EV3MediumRegulatedMotor latch = new EV3MediumRegulatedMotor(MotorPort.C);

    public static int currAngle;
    public static int currLatchPos;


    public static void initGyro(){
        gyroSensor.reset();
        System.out.println("Reset Gyro done from initGyro \n");

    }
    public static void initMotorLatchSpeed(double latchSpeed){
        //convert rad/s to degrees/s
        latchSpeed= (latchSpeed*57.2957);
        System.out.println("Defining motor speed to "+latchSpeed+" degrees/s\n");
        latch.setSpeed((int) latchSpeed);
    }

    public static void moveLatch(int dir, double angToTurn){
        double relation=5.432; //relation between degrees to turn from angle in degrees and motor position
        int angg= (int) (angToTurn*relation);
        if(dir==0){ //open latch
            latch.rotate(angg);
        }else{ //close latch
            latch.rotate((-angg));
        }

    }

    public static int currentGyroAngle(){
        final SampleProvider sp = gyroSensor.getAngleMode();

        float [] sample = new float[sp.sampleSize()]; //save sample in array
        sp.fetchSample(sample, 0); //get sample
        currAngle=(int)sample[0];

        System.out.println("Gyro angle is"+(-currAngle)+"\n");


        if(currAngle > 89){
            //latch.stop();
            System.out.println("Rotated 90 degrees from start position"+"\n");

        }
        else if(currAngle <= 0){
            //latch.stop();
            System.out.println("In start position"+"\n");

        }
        return currAngle;
    }

    public static void movement(double vel, double turnLeftMotorSpeed, double turnRightMotorSpeed, EV3LargeRegulatedMotor motorLeft, EV3LargeRegulatedMotor motorRight ){
        //begin to move

        double motorVelocity = 5 * vel * (int) Battery.getInstance().getVoltage();
        int leftSum = (int) (20 * turnLeftMotorSpeed * Battery.getInstance().getVoltage() + motorVelocity);
        int rightSum = (int) (20 * turnRightMotorSpeed * Battery.getInstance().getVoltage() + motorVelocity);


        if (leftSum < 0) {
            motorLeft.setSpeed(leftSum * (-1));
            motorLeft.backward();
        } else {
            motorLeft.setSpeed(leftSum);
            motorLeft.forward();
        }

        if (rightSum < 0) {
            motorRight.setSpeed(rightSum * (-1));
            motorRight.backward();
        } else {
            motorRight.setSpeed(rightSum);
            motorRight.forward();
        }

        // client.sendMessage("Got it");
    }

    public static void collect(double vel, double latchVelocity, EV3LargeRegulatedMotor motorLeft, EV3LargeRegulatedMotor motorRight){
        //check latch angle
        currentGyroAngle();
        if (currAngle > 10 || currAngle < 0){
            latch.rotate(currAngle * (-1));
        }


        initMotorLatchSpeed(latchVelocity);
        //åbne latch
        moveLatch(0, 90);
        //Delay.msDelay(500);
        //kørefrem
        movement(vel, 1,1,motorLeft, motorRight);
        Delay.msDelay(1000);
        motorLeft.stop();
        motorRight.stop();
        moveLatch(1, 90);
        latch.stop();
        //initMotorLatchSpeed(latchVelocity);
        //lukke latch

    }

    public static void drop( double latchVelocity, EV3LargeRegulatedMotor motorLeft, EV3LargeRegulatedMotor motorRight){

        initMotorLatchSpeed(latchVelocity);
        moveLatch(0, 90);
        movement(20, 1,1,motorLeft, motorRight);
        Delay.msDelay(1000);
        motorLeft.stop();
        motorRight.stop();
        //movement(20, -1,-1,motorLeft, motorRight);
        moveLatch(1, 90);

        latch.stop();

    }

    public static void main(final String[] args) throws IOException {
        initGyro();
        initMotorLatchSpeed(1);

        try {
            System.out.println("Foer");
            //givenGreetingClient_whenServerRespondsWhenStarted_thenCorrect();
            GreetClient client = new GreetClient();
            client.startConnection("192.168.1.129", 6666);
            //129 Julius
            //141 Chris
            //145 Ulrik
            //117 Charlotte
            //102 Robot
            final EV3LargeRegulatedMotor motorLeft = new EV3LargeRegulatedMotor(MotorPort.A);
            final EV3LargeRegulatedMotor motorRight = new EV3LargeRegulatedMotor(MotorPort.B);
            System.out.println("Efter start conn");

            double vel = 0; //robots velocity
            double turnLeftMotorSpeed = 0; //left wheel speed rad/s
            double turnRightMotorSpeed = 0; //right wheel speed rad/s
            double latchVelocity=1;


            String response;

            do {
                response = client.sendMessage("Got it");
                String[] commands = response.split(";");

                for (String command : commands) {
                    String[] commandParts = command.replace(" ", "").split("-");
                    switch (commandParts[0]) {
                        case "turn":
                            boolean cc = false;
                            for (int i = 1; i < commandParts.length; i++) {

                                if (commandParts[i].charAt(0) == 'r') {
                                    cc = false;
                                    // response = client.sendMessage("Turning Right");
                                }
                                if (commandParts[i].charAt(0) == 'l') {
                                    cc = true;
                                    //response = client.sendMessage("Turning Left");
                                }

                                if (commandParts[i].charAt(0) == 's') {
                                    if (!cc) {
                                        turnLeftMotorSpeed = Double.parseDouble(commandParts[i].substring(1));
                                        turnRightMotorSpeed = Double.parseDouble(commandParts[i].substring(1)) * -1;
                                        // response = client.sendMessage("Turn right");
                                    } else {
                                        turnLeftMotorSpeed = Double.parseDouble(commandParts[i].substring(1)) * -1;
                                        turnRightMotorSpeed = Double.parseDouble(commandParts[i].substring(1));
                                        //  response = client.sendMessage("turn left");
                                    }
                                }
                            }
                            break;

                        case "drive":
                            for (int i = 1; i < commandParts.length; i++) {
                                if (commandParts[i].charAt(0) == 's') {
                                    vel = Double.parseDouble(commandParts[i].substring(1));
                                    //  response = client.sendMessage("drive");
                                }

                                if (commandParts[i].charAt(0) == 'b') {
                                    turnRightMotorSpeed = Double.parseDouble(commandParts[i].substring(1)) * -1;
                                    turnLeftMotorSpeed = Double.parseDouble(commandParts[i].substring(1)) * -1;
                                    //  response = client.sendMessage("Back");
                                }

                            }
                            break;

                        case "collect":

                            for (int i = 1; i < commandParts.length; i++) {
                                if (commandParts[i].charAt(0) == 's') {
                                    vel = Double.parseDouble(commandParts[i].substring(1)); //drive speed

                                }

                                if (commandParts[i].charAt(0) == 'g') {
                                    latchVelocity = Double.parseDouble(commandParts[i].substring(1));
                                }

                            }
                                collect(vel, latchVelocity, motorLeft, motorRight);
                                vel = 0;
                            break;

                        case "drop":
                            drop(15, motorLeft, motorRight);
                            vel = 0;
                            break;

                        case "stop":
                            for (int i = 1; i < commandParts.length; i++) {
                                if (commandParts[i].charAt(0) == 'd') {
                                    //motorLeft.stop();
                                    //motorRight.stop();

                                    vel = 0;
                                    //response = client.sendMessage("stop drive");
                                }

                                if (commandParts[i].charAt(0) == 't') {
                                    turnLeftMotorSpeed = 0;
                                    turnRightMotorSpeed = 0;
                                    // response = client.sendMessage("stop turn");
                                }

                                if (commandParts[i].charAt(0) == 'g') {
                                    initMotorLatchSpeed(0);
                                }
                            }
                            break;
                        default:
                            break;

                    }
                }

               movement(vel, turnLeftMotorSpeed, turnRightMotorSpeed, motorLeft, motorRight);
                System.out.println(response);



            } while (!response.equals("exit"));


        /*
        System.out.println("Creating Motor A & B & C");
        final EV3LargeRegulatedMotor motorLeft = new EV3LargeRegulatedMotor(MotorPort.A);
        final EV3LargeRegulatedMotor motorRight = new EV3LargeRegulatedMotor(MotorPort.B);
        final EV3MediumRegulatedMotor hatch = new EV3MediumRegulatedMotor(MotorPort.C);

        //To Stop the motor in case of pkill java for example
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.out.println("Emergency Stop");
                motorLeft.stop();
                motorRight.stop();
            }
        }));


        System.out.println("Defining the Stop mode");
        motorLeft.brake();
        motorRight.brake();

        System.out.println("Defining motor speed");
        final int motorSpeed = 100 * (int) Battery.getInstance().getVoltage();
        motorLeft.setSpeed(motorSpeed);
        motorRight.setSpeed(motorSpeed);
        hatch.setSpeed(180);


        System.out.println("Go Forward with the motors");
        //motorLeft.forward();
        //motorRight.forward();
        hatch.backward();

        Delay.msDelay(500);

        hatch.stop();
        motorLeft.forward();
        motorRight.forward();

        Delay.msDelay(1800);
        System.out.println("Stop motors");
        motorLeft.stop();
        motorRight.stop();
        hatch.stop();

        System.out.println("Go Backward with the motors");
        motorLeft.backward();
        motorRight.backward();
        //hatch.backward();

        Delay.msDelay(200);

        System.out.println("Stop motors");
        motorLeft.stop();
        motorRight.stop();
        hatch.stop();

        System.out.println("Checking Battery");
        System.out.println("Votage: " + Battery.getInstance().getVoltage());
*/

            System.exit(0);
        }catch(Exception e){
            System.out.println(e);
            while(true){

            }

        }



    }
}
