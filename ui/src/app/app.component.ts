import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface PendingRequest {
  pendingRequest: string;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  private _pendingRequest: PendingRequest;

  constructor(private _httpClient: HttpClient) {

  }

  get pendingRequest() {
    return this._pendingRequest;
  }

  signDocument() {
    const location = window.location;
    const completationUrl = location .protocol + '//' + location.host + '/' + location.pathname.split('/')[1];
    this._httpClient.post<PendingRequest>('/api/sign-document', {completionUrl: completationUrl})
      .subscribe(pendingRequest => {
        this._pendingRequest = pendingRequest;
        setTimeout(() => document.forms['BrowserPostForm'].submit(), 100);
      });
  }


}
