package sample;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CutScreen extends Application {
    private BufferedImage bufferedImage = null;
    private Stage stage = new Stage(); //拖拽窗口
    private AnchorPane anchorPane = new AnchorPane(); //截屏背景
    private Scene scene = new Scene(anchorPane); //设置场景

    private double start_x; //记录截图开始x
    private double start_y; //记录截屏开始y
    private double end_x; //记录截图结束x
    private double end_y; //记录截屏结束y
    private HBox hBox = new HBox();  //用于拖拽框
    private Label label = new Label(); //拖拽大小提示框,用于显示截图大小
    private Button finishBtn = new Button("保存"); //完成截图按钮
    private Button cancelBtn = new Button("取消"); //取消按钮

    private double real_x; //拖拽实时坐标x
    private double real_y; //拖拽实时坐标y
    public void initView(){

        stage.setIconified(true);
        anchorPane.setStyle("-fx-background-color: #B5B5B522");

        scene.setFill(Paint.valueOf("#ffffff00"));
        stage.setScene(scene);
        stage.setFullScreen(true);// 全屏
        stage.setWidth(Screen.getPrimary().getBounds().getWidth());
        stage.setHeight(Screen.getPrimary().getBounds().getHeight());
        stage.initStyle(StageStyle.TRANSPARENT);

        hBox.setStyle("-fx-background-color: #ffffff00; -fx-border-width: 1; -fx-border-color: blue");
        label.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff");
        stage.show();
        this.initEvent();
    }

    public void initEvent(){
        anchorPane.getChildren().add(hBox);
        //设置ESC退出截屏窗口a
        scene.setOnKeyPressed(p -> {
            if (p.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });

        anchorPane.setOnMousePressed(p->{
            label.setVisible(true);
            anchorPane.getChildren().clear();
            hBox.setPrefWidth(0);
            hBox.setPrefHeight(0);
            label.setText("宽度: 0 高度: 0");

            //记录拖拽开始坐标
            start_x = p.getSceneX();
            start_y = p.getSceneY();

            //给拖拽框设置起始位置
            AnchorPane.setLeftAnchor(hBox, start_x);
            AnchorPane.setTopAnchor(hBox, start_y );

            //给信息提示框设置位置
            label.setLayoutX(start_x);
            label.setLayoutY(start_y - label.getHeight());

            //给拖拽窗口添加元素
            anchorPane.getChildren().add(hBox);
            anchorPane.getChildren().add(label);

            //添加完成截屏按钮,默认开始不显示
            finishBtn.setVisible(false);
            cancelBtn.setVisible(false);
            anchorPane.getChildren().addAll(finishBtn, cancelBtn);
        });

        //设置拖拽检测
        anchorPane.setOnDragDetected(dragDetected->anchorPane.startFullDrag());

        //设置拖拽事件
        anchorPane.setOnMouseDragOver(p -> {
            //获取实时拖拽的坐标
            end_x = p.getSceneX();
            end_y = p.getSceneY();

            //获取拖拽实时大小
            real_x = Math.abs(end_x - start_x);
            real_y = Math.abs(end_y - start_y);

            //给拖拽框设置大小
            getAnchorPane();
            hBox.setPrefWidth(real_x);
            hBox.setPrefHeight(real_y);

            //给提示框设置大小信息
            label.setText("宽度: " + real_x + " 高度: " + real_y);

        });

        //设置拖拽结束事件
        anchorPane.setOnMouseDragReleased(p ->{
            //设置完成截屏按钮位置
            anchorPane.setStyle("-fx-background-color: #00000000");
            getFinishButtonLocation();
            //结束是显示完成截图按钮
            finishBtn.setVisible(true);
            cancelBtn.setVisible(true);
        });

        //设置完成按钮事件
        finishBtn.setOnAction(p ->{
            try {
                getScreenImageBuffer();
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                oneOperationResult();
            }

        });

        //取消按钮
        cancelBtn.setOnAction(event -> {
            oneOperationResult();
        });
    }

    /**
     * 每次截屏后恢复最初状态
     */
    public void oneOperationResult(){
        label.setVisible(false);
        hBox.setPrefHeight(0);
        hBox.setPrefWidth(0);
        finishBtn.setVisible(false);
        cancelBtn.setVisible(false);
        anchorPane.setStyle("-fx-background-color: #B5B5B522");
    }

    /**
     * 设置取消和保存按钮的位置
     */
    public void getFinishButtonLocation(){
        if (start_x > end_x && start_y > end_y){//左上角
            AnchorPane.setLeftAnchor(finishBtn,start_x - 2*finishBtn.getWidth());
            AnchorPane.setTopAnchor(finishBtn,start_y + finishBtn.getHeight());
            AnchorPane.setLeftAnchor(cancelBtn,start_x - finishBtn.getWidth());
            AnchorPane.setTopAnchor(cancelBtn,start_y + finishBtn.getHeight());
        } else if (start_x > end_x && start_y < end_y){ //左下角
            AnchorPane.setLeftAnchor(finishBtn,start_x - 2*finishBtn.getHeight());
            AnchorPane.setTopAnchor(finishBtn,end_y + finishBtn.getHeight());
            AnchorPane.setLeftAnchor(cancelBtn,start_x - 2*finishBtn.getHeight());
            AnchorPane.setTopAnchor(cancelBtn,end_y + finishBtn.getHeight());
        }else if (start_y > end_y && start_x < end_x){//右上角
            AnchorPane.setLeftAnchor(finishBtn,end_x - 2*finishBtn.getWidth());
            AnchorPane.setTopAnchor(finishBtn,start_y + finishBtn.getHeight());
            AnchorPane.setLeftAnchor(cancelBtn,end_x - finishBtn.getWidth());
            AnchorPane.setTopAnchor(cancelBtn,start_y + finishBtn.getHeight());
        }else {//右下角
            AnchorPane.setLeftAnchor(finishBtn,end_x - 2*finishBtn.getWidth());
            AnchorPane.setTopAnchor(finishBtn,end_y + finishBtn.getHeight());
            AnchorPane.setLeftAnchor(cancelBtn,end_x - finishBtn.getWidth());
            AnchorPane.setTopAnchor(cancelBtn,end_y + finishBtn.getHeight());
        }
    }

    /**
     * 设置截屏区域的位置
     */
    public void getAnchorPane(){
        if (start_x > end_x && start_y > end_y){//左上角
            AnchorPane.setLeftAnchor(hBox, end_x);
            AnchorPane.setTopAnchor(hBox, end_y );
        } else if (start_x > end_x && start_y < end_y){ //左下角
            AnchorPane.setLeftAnchor(hBox, end_x);
            AnchorPane.setTopAnchor(hBox, start_y);
        }else if (start_y > end_y && start_x < end_x){//右上角
            AnchorPane.setLeftAnchor(hBox, start_x);
            AnchorPane.setTopAnchor(hBox, end_y );
        }else {//右下角
            AnchorPane.setLeftAnchor(hBox, start_x);
            AnchorPane.setTopAnchor(hBox, start_y );
        }
    }

    /**
     * 使用截屏工具根据位置截取出矩形大小的图片
     * @throws Exception
     */
    private void getScreenImageBuffer() throws Exception {
        Robot robot = new Robot();
        //创建一个矩形,使用截屏工具根据位置截取出矩形大小的图片
        Rectangle rectangle = getRectangle();
        bufferedImage = robot.createScreenCapture(rectangle);
        saveScreenImage();
    }

    /**
     * 获取截屏区域，减一去掉周围边框
     * @return
     */
    public Rectangle getRectangle(){
        Rectangle rectangle;
        if (start_x  < end_x && start_y < end_y){
            rectangle = new Rectangle((int) start_x + 1, (int) start_y + 1 , (int) real_x - 1, (int) real_y -1);
        }else if (start_x  > end_x && start_y < end_y){
            rectangle = new Rectangle((int) end_x + 1, (int) start_y + 1, (int) real_x - 1, (int) real_y - 1);
        }else if (start_x  < end_x && start_y > end_y){
            rectangle = new Rectangle((int) start_x + 1, (int) end_y + 1, (int) real_x - 1, (int) real_y - 1);
        }else {
            rectangle = new Rectangle((int) end_x + 1, (int) end_y + 1 , (int) real_x - 1, (int) real_y - 1);
        }

        return rectangle;
    }

    /**
     * 获取image图片
     * @return
     */
    public WritableImage getScreenImage(){
        if (bufferedImage == null){
            return null;
        }
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }


    /**
     * 保存图片
     * @throws IOException
     */
    public void saveScreenImage() throws IOException {
        if (bufferedImage == null){
            return;
        }
        //获取系统剪切板,存入截图
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putImage(getScreenImage());
        clipboard.setContent(content);
        //输出到桌面
        ImageIO.write(bufferedImage, "png",
                new File("C:\\Users\\Beimo\\Desktop\\" + System.currentTimeMillis() + ".png"));
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.initView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
