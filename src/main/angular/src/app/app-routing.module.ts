import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReturnComponent } from './return/return.component';
import { CheckoutComponent } from './checkout/checkout.component';

const routes: Routes = [
  { path: '', component: CheckoutComponent },
  { path: 'return', component: ReturnComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
