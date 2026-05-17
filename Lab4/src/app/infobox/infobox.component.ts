import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-infobox',
  standalone: true,
  imports: [],
  templateUrl: './infobox.component.html',
  styleUrl: './infobox.component.css'
})
export class InfoboxComponent {
  @Input() position: any;
}