/*
 * Copyright (c) 2008, 2011 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package dataapppreloader;

import javafx.animation.PathTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Fun RaceTrack Progress Indicator
 */
public class RaceTrack extends Pane {
    private static final String trackPath = "M636.606,425.946c-20.779,0-172.295-0.027-172.295-0.027H347.428"
            + "c0,0,0,0-19.48,0s-106.494,3.031-138.095-54.545S48.294,193.019,109.766,119.859s103.463-74.892,151.082-48.918"
            + "s29.525,163.28,80.087,184.416c48.918,20.449,42.424-18.513,86.147-4.329c27.273,10.059,38.096,39.496,61.473,45.021"
            + "c30.963,7.319,65.757-22.646,77.056-69.594c25.107-104.329-3.929-132.62,48.917-174.129"
            + "c42.857-33.663,124.243-10.316,145.022,39.93c23.439,56.679,24.611,96.368,15.898,145.671"
            + "C767.845,280.955,756.952,427.754,636.606,425.946z";

    private PathTransition race;
    private Text percentage;
    private final DoubleProperty progress = new SimpleDoubleProperty() {
        @Override protected void invalidated() {
            final double progress = get();
            if (progress >= 0) {
                race.jumpTo(Duration.seconds(progress));
                percentage.setText(((int)(100d*progress))+"%");
            }
        }
    };
    public double getProgress() { return progress.get(); }
    public void setProgress(double value) { progress.set(value); }
    public DoubleProperty progressProperty() { return progress; }

    public RaceTrack() {
        SVGPath road = new SVGPath();
        road.setContent(trackPath);
        road.setFill(null);
        road.setStroke(Color.gray(0.4));
        road.setStrokeWidth(50);
        road.setEffect(new DropShadow(BlurType.ONE_PASS_BOX, Color.BLACK, 20, 0, 0, 0));

        SVGPath trackLine = new SVGPath();
        trackLine.setContent(trackPath);
        trackLine.setFill(null);
        trackLine.setStroke(Color.WHITE);
        trackLine.getStrokeDashArray().addAll(8d, 6d);

        Line startLine = new Line(610.312, 401.055, 610.312, 450.838);
        startLine.setStroke(Color.WHITE);
        startLine.getStrokeDashArray().addAll(2d, 2d);

        Text startFinish = new Text(570, 475, "START/FINISH");
        startFinish.setFill(Color.WHITE);

        percentage = new Text(390, 170, "0%");
        percentage.setFont(Font.font("System", 60));
        percentage.setFill(Color.web("#ddf3ff"));
        percentage.setStroke(Color.web("#73c0f7"));
        percentage.setEffect(new DropShadow(BlurType.ONE_PASS_BOX, Color.web("#3382ba"), 15, 0, 0, 0));

        ImageView raceCarImg = new ImageView(new Image(
                DataAppPreloader.class.getResourceAsStream("images/Mini-red-and-white.png")));
        raceCarImg.setX(raceCarImg.getImage().getWidth()/2);
        raceCarImg.setY(raceCarImg.getImage().getHeight()/2);
        raceCarImg.setRotate(90);
        Group raceCar = new Group(raceCarImg);

        Group track = new Group(road, trackLine, startLine, startFinish);
        track.setCache(true);
        // add children
        getChildren().addAll(track, raceCar, percentage);
        
        // Create path animation that we will use to drive the car along the track
        race = new PathTransition(Duration.seconds(1), road, raceCar);
        race.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        race.play();
        race.pause();
        
        // center our content and set our size
        setTranslateX(-road.getBoundsInLocal().getMinX());
        setTranslateY(-road.getBoundsInLocal().getMinY());
        setPrefSize(road.getBoundsInLocal().getWidth(), road.getBoundsInLocal().getHeight());
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
    }
}
