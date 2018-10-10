package com.sgrh.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.sgrh.bean.Visitor;
import com.sgrh.dao.VisitorDAOImp;
import com.sgrh.fineupload.io.StorageService;
import com.sgrh.fineupload.model.UploadRequest;
import com.sgrh.fineupload.model.UploadResponse;

@Controller
public class GatePassMainController {
	
	@Autowired
	private StorageService storageService;
	@Autowired 
	VisitorDAOImp visitorDao;
	
	
	@RequestMapping("/")
	public String mainForm(Model model) {
		Visitor visitor = new Visitor();
		visitor.setVisitDate(LocalDate.now());
		visitor.setVisitTime(LocalTime.now());
		model.addAttribute("visitor",visitor);
		return "index";
	}
	/*
	@RequestMapping("process")
	public String processForm(@Valid @ModelAttribute("visitor")Visitor visitor, BindingResult result) {
		String target_page = "";
		if(result.hasErrors()) {
			System.out.println(result.getErrorCount());
			for(FieldError error: result.getFieldErrors()) {
				System.out.println(error.getDefaultMessage());
			}
			target_page = "index";
		}
		else {
			target_page = "processed_form";
		}
		return target_page;
	}*/
	
	@RequestMapping("save")
	public String saveForm(Model model) {
		return "save_visitor_data";
	}
	
	@GetMapping("search")
	public String searchForm(Model model) {
		return "search_page";
	}
	
	
	@RequestMapping("printing")
	public String printBarcode(Model model, @ModelAttribute("visitor") Visitor visitor) {
		System.out.println(visitor.getName());
		return "barcode_printing";
	}
	
	@RequestMapping("processed")
	public String fileUploader(Model model, @RequestParam("id") int id) {
		Visitor visitor = visitorDao.getVisitor(id);
		model.addAttribute("visitor",visitor);
		return "processed_form";
	}
	
	@PostMapping(value="uploadParser")
	@ResponseBody
	  public void upload(
	            @RequestParam("qqfile") MultipartFile file,
	            @RequestParam("qquuid") String uuid,
	            @RequestParam("qqfilename") String fileName,
	            @RequestParam(value = "qqpartindex", required = false, defaultValue = "-1") int partIndex,
	            @RequestParam(value = "qqtotalparts", required = false, defaultValue = "-1") int totalParts,
	            @RequestParam(value = "qqtotalfilesize", required = false, defaultValue = "-1") long totalFileSize,
	            HttpServletRequest req, @Valid @ModelAttribute Visitor visitor, BindingResult result,HttpServletResponse response) {
			String targetPath = null;
			try {
				System.out.println(visitor.getName());
				System.out.println("qquuid "+uuid);
				System.out.println("qqfilename"+fileName);
		        UploadRequest request = new UploadRequest(uuid, file);
		        request.setFileName(fileName);
		        request.setTotalFileSize(totalFileSize);
		        request.setPartIndex(partIndex);
		        request.setTotalParts(totalParts);
		        storageService.save(request,req);
		        
		        visitor.setImagePath(uuid.concat("/").concat(fileName));
		        System.out.println(visitorDao.saveVisitor(visitor));
		        targetPath = "processed_form";
			}
			catch(Exception ex) {
				System.out.println("Exception Thrown");
				targetPath = "index";
			}
			JSONObject response_obj = new JSONObject();
			try {
				response_obj.put("success",true);
				response_obj.put("visitor_id", visitor.getId());
				response.setCharacterEncoding("UTF-8");
				response.setContentType("text");
				response.getWriter().print(response_obj);
				response.flushBuffer();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	    }
}
