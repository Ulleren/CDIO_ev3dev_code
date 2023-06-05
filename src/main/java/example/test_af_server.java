package example;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import ev3dev.actuators.lego.motors.EV3MediumRegulatedMotor;
import ev3dev.sensors.Battery;

import ev3dev.sensors.ev3.EV3TouchSensor;
import lejos.hardware.port.MotorPort;
import ev3dev.robotics.tts.Espeak;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class test_af_server{


    public static void main(String[] args) throws IOException {

            Server server = new Server();
            server.start(6666);
            server.stop();
    }
}

class Server{

    private ServerSocket serverSocket;
    private Socket clientSocket;

   private static PrintWriter out;
   private static BufferedReader in;


    //static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S1);
    private static EV3TouchSensor touch1 = new EV3TouchSensor(SensorPort.S1);
    final EV3LargeRegulatedMotor motorLeft = new EV3LargeRegulatedMotor(MotorPort.A);
    final EV3LargeRegulatedMotor motorRight = new EV3LargeRegulatedMotor(MotorPort.B);
    static EV3MediumRegulatedMotor latch = new EV3MediumRegulatedMotor(MotorPort.C);

    final SampleProvider sp = touch1.getTouchMode();

    public static int currAngle;
    int max_speed = 1;
    Boolean overwrite = false;
/*
    public static void initGyro(){
        gyroSensor.reset();
        System.out.println("Reset Gyro done from initGyro \n");

    }
    */

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

    /*
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

     */

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
       /* currentGyroAngle();
        if (currAngle > 10 || currAngle < 0){
            latch.rotate(currAngle * (-1));
        }
*/

        initMotorLatchSpeed(latchVelocity);
        //åbne latch
        moveLatch(0, 90);
        //Delay.msDelay(500);
        //kørefrem
        movement(vel, 1,1,motorLeft, motorRight);
        Delay.msDelay(1500);
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
        movement(12, 1,1,motorLeft, motorRight);
        Delay.msDelay(750);
        motorLeft.stop();
        motorRight.stop();
        //movement(20, -1,-1,motorLeft, motorRight);
        //moveLatch(1, 90);

        latch.stop();
        //ca. 30-36 cm afstand fra målet
    }


    public void start(int port) throws IOException {
            serverSocket = new ServerSocket(port);
            String response;
            int stopCount = 0;


            System.out.println("ready to recive");
            voice("what is my purpose");
            clientSocket = serverSocket.accept();
            System.out.println("er her");


            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //GreetClient client = new GreetClient();



            //initGyro();
            initMotorLatchSpeed(1);
            // latch.rotate(90);
            double vel = 0; //robots velocity
            double turnLeftMotorSpeed = 0; //left wheel speed rad/s
            double turnRightMotorSpeed = 0; //right wheel speed rad/s
            double latchVelocity = 1;
            double langle;
            int mangle;


            do {
                out.println("Got it");
               //out.println("latch angle = " + currAngle);
                response = "N/A";
                while (in.ready()) {
                    response = in.readLine();
                }

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
                                    if (max_speed < vel && overwrite == false){
                                        vel = max_speed;
                                        max_speed++;
                                    }
                                    //  response = client.sendMessage("drive");
                                }

                                if (commandParts[i].charAt(0) == 'b') {
                                    turnRightMotorSpeed = Double.parseDouble(commandParts[i].substring(1)) * -1;
                                    turnLeftMotorSpeed = Double.parseDouble(commandParts[i].substring(1)) * -1;
                                    //  response = client.sendMessage("Back");
                                }

                                if (commandParts[i].charAt(0) == 'o') {
                                    overwrite = true;
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

                        case "gate":
                            for (int i = 1; i < commandParts.length; i++) {
                                if (commandParts[i].charAt(0) == 'u') { //move latch up
                                    langle = Double.parseDouble(commandParts[i].substring(1));
                                    moveLatch(0, langle);
                                }

                                if (commandParts[i].charAt(0) == 'd') { //move latch down
                                    langle = Double.parseDouble(commandParts[i].substring(1));
                                    moveLatch(1, langle);
                                }

                                if (commandParts[i].charAt(0) == 'a') { //move latch to custom angle
                                    langle = Double.parseDouble(commandParts[i].substring(1));
                                    //currentGyroAngle();
                                    mangle = (int) (langle - currAngle);
                                    latch.rotate(mangle);
                                }

                                if (commandParts[i].charAt(0) == 's') { //drive speed
                                    vel = Double.parseDouble(commandParts[i].substring(1));

                                }

                                if (commandParts[i].charAt(0) == 'g') { //gate speed
                                    latchVelocity = Double.parseDouble(commandParts[i].substring(1));
                                }

                            }
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
                                    max_speed = 1;
                                    overwrite = false;
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
                if (!response.equals("N/A")) {
                    System.out.println(response);
                   /* if(stopCount++ >= 50){
                        System.out.println("waiting to accept new connection");
                        clientSocket = serverSocket.accept();
                        System.out.println("accepted new connection");
                        stopCount = 0;
                    }
                }else{
                    stopCount = 0;*/
                }

                    response.equals(escape());
            } while (!response.equals("exit"));
                 voice("Bye bitches");
            do {
                Server server = new Server();
                server.start(6666);
                server.stop();
            }while (!response.equals("off"));

        }


    public void stop() throws IOException {
        clientSocket.close();
        serverSocket.close();
        in.close();
        out.close();
    }
    public void voice (String arg){
        Espeak TTS = new Espeak();

        TTS.setVoice("en");
        TTS.setSpeedReading(105);
        TTS.setPitch(60);
        TTS.setMessage(arg);
        TTS.say();
    }

    public String escape(){
        final SampleProvider sp = touch1.getTouchMode();
        int touchValue = 0;

        //Control loop
        final int iteration_threshold = 20;
        for(int i = 0; i <= iteration_threshold; i++) {

            float [] sample = new float[sp.sampleSize()];
            sp.fetchSample(sample, 0);
            touchValue = (int) sample[0];


        }
        if(touchValue > 15){
            return "exit";
        }else return " ";
    }

}
