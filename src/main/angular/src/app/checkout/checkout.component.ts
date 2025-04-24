import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Product } from '../models/product.model';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})

export class CheckoutComponent {

  apiUrl = environment.apiUrl;
  products: Product[] = [];
  cart: any[] = [];
  customerName: string = '';
  address: string = '';
  amount: number = 0;

  ngOnInit(): void {
    this.http.get<any>(`${this.apiUrl}/products`)
      .subscribe(response => {
        if (response.status === 200) {
          this.products = response.data;  // Lấy dữ liệu sản phẩm từ response
        } else {
          // Xử lý khi có lỗi (nếu cần)
          console.error('Lỗi: ', response.message);
        }
      });
  }

  constructor(private http: HttpClient) {}

  addToCart(product: any) {
    const existing = this.cart.find(item => item.name === product.name);
    if (existing) {
      existing.quantity++;
    } else {
      this.cart.push({ ...product, quantity: 1 });
    }
  }

  getTotal() {
    this.amount = this.cart.reduce((sum, item) => sum + item.price * item.quantity, 0)
    return this.amount;
  }

  checkout() {
    const payload = {
      customerName: this.customerName,
      address: this.address,
      items: this.cart,
      amount: this.amount
    };

    this.http.post<any>(`${this.apiUrl}/payment/create/vnpay`, payload)
    .subscribe(res => {
      if (res && res.redirectUrl) {
        window.location.href = res.redirectUrl;
      }
    });
  }
}
