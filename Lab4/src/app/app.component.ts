import { Component } from '@angular/core';
import { AppMapComponent } from './app-map/app-map.component';
import { InfoboxComponent } from './infobox/infobox.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [AppMapComponent, InfoboxComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  position: any = null;

  appPositionChanged(pos: any) {
    this.position = pos;
  }
}