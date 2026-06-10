import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {

  private COMPNAME:any = "App Component";

  private SUperDatas:any = {
    "name": "John Doe",
    "email": "abcd@gmail.com"
  }
}
