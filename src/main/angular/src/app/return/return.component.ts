import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-return',
  templateUrl: './return.component.html',
  styleUrls: ['./return.component.css']
})
export class ReturnComponent implements OnInit {
  paymentStatus: string = '';
  responseCode: string | null = '';
  txnRef: string | null = '';

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.responseCode = params['vnp_ResponseCode'];
      this.txnRef = params['vnp_TxnRef'];

      if (this.responseCode === '00') {
        this.paymentStatus = 'Thanh toán thành công 🎉';
      } else {
        this.paymentStatus = 'Thanh toán thất bại ❌';
      }
    });
  }
}
