import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import OlMap from 'ol/Map';
import OlTileLayer from 'ol/layer/Tile';
import OlXYZ from 'ol/source/XYZ';
import OlView from 'ol/View';
import Point from 'ol/geom/Point';
import Feature from 'ol/Feature';
import VectorSource from 'ol/source/Vector';
import VectorLayer from 'ol/layer/Vector';
import { fromLonLat, toLonLat } from 'ol/proj';
import { Icon, Style } from 'ol/style';
import 'ol/ol.css';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [],
  templateUrl: './app-map.component.html',
  styleUrl: './app-map.component.css'
})
export class AppMapComponent implements OnInit {
  tmcMap: any;
  tmcSource: any;
  tmcLayer: any;
  tmcView: any;
  tmcVectorSource: any;
  tmcVectorLayer: any;

  @Output() positionChanged = new EventEmitter<any>();

  ngOnInit(): void {
    const myPoint = new Point([18.3, 54.5]);

    this.tmcSource = new OlXYZ({
      url: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png'
    });

    this.tmcLayer = new OlTileLayer({
      source: this.tmcSource
    });

    this.tmcVectorSource = new VectorSource({});
    this.tmcVectorLayer = new VectorLayer({
      source: this.tmcVectorSource
    });

    this.tmcView = new OlView({
      center: fromLonLat(myPoint.getCoordinates()),
      zoom: 13
    });

    this.tmcMap = new OlMap({
      target: 'map',
      layers: [this.tmcLayer, this.tmcVectorLayer],
      view: this.tmcView
    });

    // click - center map and add point
    this.tmcMap.on('click', (event: any) => {
      const coords = this.tmcMap.getCoordinateFromPixel(event.pixel);
      this.tmcView.animate({
        center: coords,
        duration: 1000
      });
      
      const pointFeature = new Feature(new Point(coords));
      pointFeature.setStyle(new Style({
        image: new Icon({
          anchor: [0.5, 1],
          src: 'https://openlayers.org/en/latest/examples/data/icon.png'
        })
      }));
      this.tmcVectorSource.addFeature(pointFeature);
    });

    // pointermove - emit position to parent
    this.tmcMap.on('pointermove', (event: any) => {
      if (event.dragging) return;
      const coords = this.tmcMap.getCoordinateFromPixel(event.pixel);
      const lonLat = toLonLat(coords);
      this.positionChanged.emit(lonLat);
    });
  }
}