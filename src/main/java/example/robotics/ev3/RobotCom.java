package example.robotics.ev3;

import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import ev3dev.actuators.lego.motors.EV3MediumRegulatedMotor;
import ev3dev.sensors.Battery;
import example.GreetClient;
import lejos.hardware.port.MotorPort;
import lejos.utility.Delay;
import java.io.IOException;

public class RobotCom {
    public static void main(final String[] args) throws IOException {
        try {
            System.out.println("Foer");
            //givenGreetingClient_whenServerRespondsWhenStarted_thenCorrect();
            GreetClient client = new GreetClient();
            client.startConnection("192.168.1.129", 6666);
            System.out.println("Efter start conn");

            final EV3LargeRegulatedMotor motorLeft = new EV3LargeRegulatedMotor(MotorPort.A);
            final EV3LargeRegulatedMotor motorRight = new EV3LargeRegulatedMotor(MotorPort.B);
            final EV3MediumRegulatedMotor hatch = new EV3MediumRegulatedMotor(MotorPort.C);
            final int motorSpeed = 100 * (int) Battery.getInstance().getVoltage();
            double vel = 0;
            double motorVelocity;
            double turnLeftMotorSpeed = 0;
            double turnRightMotorSpeed = 0;
            int leftSum;
            int rightSum;
            boolean hatchFlag = true;

            motorLeft.setSpeed(motorSpeed);
            motorRight.setSpeed(motorSpeed);
            hatch.setSpeed(180);
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
                                if (commandParts[i].charAt(0) == 'g') {
                                    if (!hatchFlag){
                                        hatch.backward();
                                        Delay.msDelay(500);
                                        hatch.stop();
                                        hatchFlag = true;
                                    }
                                    turnLeftMotorSpeed = 0;
                                    turnRightMotorSpeed = 0;

                                    vel = 10;
                                    motorLeft.forward();
                                    motorRight.forward();
                                    Delay.msDelay(1000);
                                    motorLeft.stop();
                                    motorRight.stop();
                                    hatch.forward();
                                    Delay.msDelay(500);
                                    hatch.stop();

                                    vel = Double.parseDouble(commandParts[i].substring(1));
                                    //  response = client.sendMessage("gate");
                                }

                                if (commandParts[i].charAt(0) == 'h') {
                                    if (hatchFlag){
                                        hatch.forward();
                                        Delay.msDelay(500);
                                        hatch.stop();
                                        hatchFlag = false;
                                    }
                                }

                            }
                            break;


                        case "stop":
                            for (int i = 1; i < commandParts.length; i++) {
                                if (commandParts[i].charAt(0) == 'd') {
                                    motorLeft.stop();
                                    motorRight.stop();
                                    vel = 0;
                                    //response = client.sendMessage("stop drive");
                                }

                                if (commandParts[i].charAt(0) == 't') {
                                    turnLeftMotorSpeed = 0;
                                    turnRightMotorSpeed = 0;
                                    // response = client.sendMessage("stop turn");
                                }

                                if (commandParts[i].charAt(0) == 'g') {
                                    hatch.stop();
                                    //response = client.sendMessage("stop hatch");
                                }
                            }
                            break;
                        default:
                            break;

                    }
                }

                motorVelocity = 5 * vel * (int) Battery.getInstance().getVoltage();
                leftSum = (int) (20 * turnLeftMotorSpeed * Battery.getInstance().getVoltage() + motorVelocity);
                rightSum = (int) (20 * turnRightMotorSpeed * Battery.getInstance().getVoltage() + motorVelocity);


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

                System.out.println(response);
                // client.sendMessage("Got it");
            } while (!response.equals("exit"));

            System.out.println("Checking Battery");
            System.out.println("Votage: " + Battery.getInstance().getVoltage());

            System.exit(0);
        }catch(Exception e){
            System.out.println(e);
            while(true){

            }

        }
    }
    static void execCom( int left, int right, int speed){

    }
}
