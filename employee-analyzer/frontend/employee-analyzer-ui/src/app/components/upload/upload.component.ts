import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common'; // ✅ needed for built-in pipes

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [CommonModule], // ✅ enables |json and *ngIf, *ngFor
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.css']
})
export class UploadComponent {
  file: File | null = null;
  response: any = null;

  constructor(private http: HttpClient) {}

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.file = input.files[0];
      console.log('File selected:', this.file.name);
    }
  }

  onUpload() {
    if (!this.file) {
      alert('Please select a file.');
      return;
    }

    const formData = new FormData();
    formData.append('file', this.file);

    this.http.post('http://localhost:8080/api/upload', formData).subscribe({
      next: (res: any) => {
        console.log('📦 Response from backend:', res);
        this.response = res;
      },
      error: (err) => {
        console.error('Upload error:', err);
        alert('Upload failed.');
      }
    });
  }
}




